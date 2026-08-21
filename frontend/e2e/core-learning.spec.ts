import { expect, test, type Page } from '@playwright/test';

async function enabledQuestionId(page: Page): Promise<number> {
  const response = await page.request.get('/api/questions?page=1&pageSize=1&status=ENABLED');
  expect(response.ok()).toBeTruthy();
  const body = await response.json() as { data: { items: Array<{ id: number }> } };
  const questionId = body.data.items[0]?.id;
  expect(questionId).toBeTruthy();
  return questionId;
}

function collectErrors(page: Page): string[] {
  const errors: string[] = [];
  page.on('console', (message) => {
    if (message.type() === 'error') errors.push(message.text());
  });
  page.on('pageerror', (error) => errors.push(error.message));
  page.on('response', (response) => {
    if (response.status() >= 400) errors.push(`${response.status()} ${response.url()}`);
  });
  return errors;
}

async function verifyViewport(page: Page, route: string, heading: string, width: number, height: number) {
  await page.setViewportSize({ width, height });
  await page.emulateMedia({ reducedMotion: 'reduce' });
  await page.goto(route);
  await expect(page.getByRole('heading', { name: heading })).toBeVisible();
  await page.screenshot({ fullPage: false, animations: 'disabled' });
}

test('Workbench、Knowledge、Question 生成桌面与移动端基线', async ({ page }) => {
  const errors = collectErrors(page);
  const questionId = await enabledQuestionId(page);
  await verifyViewport(page, '/', '首页 / 工作台', 1720, 1000);
  await verifyViewport(page, '/knowledge', '知识地图', 1720, 1000);
  await verifyViewport(page, `/questions/${questionId}`, '题目学习', 1720, 1000);
  await verifyViewport(page, '/', '首页 / 工作台', 390, 844);
  await verifyViewport(page, `/questions/${questionId}`, '题目学习', 390, 844);
  expect(errors).toEqual([]);
});

test('练习模式不泄漏答案并真实提交答题', async ({ page }) => {
  const errors = collectErrors(page);
  const questionId = await enabledQuestionId(page);
  let workspacePayload: Record<string, unknown> | undefined;
  page.on('response', async (response) => {
    if (response.url().endsWith(`/api/v1/questions/${questionId}`) && response.request().method() === 'GET') {
      const body = await response.json() as { data: Record<string, unknown> };
      workspacePayload = body.data;
    }
  });

  await page.goto(`/questions/${questionId}`);
  await expect(page.getByRole('button', { name: '开始练习' })).toBeVisible();
  expect(workspacePayload).toBeDefined();
  expect(workspacePayload).not.toHaveProperty('answers');
  expect(workspacePayload).not.toHaveProperty('plainExplanation');

  await page.getByRole('button', { name: '开始练习' }).click();
  await page.getByLabel('我的回答').fill('这是 P05 端到端练习回答。');
  await page.getByRole('button', { name: '查看参考答案' }).click();
  await expect(page.getByText('30 秒回答')).toBeVisible();
  await page.getByText('部分正确', { exact: true }).click();
  await page.getByRole('button', { name: '提交练习' }).click();
  await expect(page.getByText('本次练习已保存')).toBeVisible();
  await page.reload();
  await expect(page.getByRole('button', { name: '开始练习' })).toBeVisible();
  expect(errors).toEqual([]);
});

test('笔记串行自动保存并可刷新恢复', async ({ page }) => {
  const errors = collectErrors(page);
  const questionId = await enabledQuestionId(page);
  const noteContent = `P05 自动保存验收 ${Date.now()}`;
  await page.goto(`/questions/${questionId}`);
  const editor = page.getByLabel('学习笔记');
  await editor.fill(noteContent);
  await expect(page.getByRole('status')).toHaveText('已保存', { timeout: 8_000 });
  await page.reload();
  await expect(page.getByLabel('学习笔记')).toHaveValue(noteContent);
  expect(errors).toEqual([]);
});
