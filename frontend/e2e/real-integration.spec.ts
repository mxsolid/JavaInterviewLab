import { randomUUID } from 'node:crypto';
import { expect, test, type Page } from '@playwright/test';

function collectUnexpectedErrors(page: Page): string[] {
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

async function firstEnabledQuestionId(page: Page): Promise<number> {
  const response = await page.request.get('/api/questions?page=1&pageSize=1&status=ENABLED');
  expect(response.ok()).toBeTruthy();
  const body = await response.json() as { data: { items: Array<{ id: number }> } };
  return body.data.items[0].id;
}

test('生产 routes 使用真实 API 完成学习与系统状态联调', async ({ page }) => {
  const errors = collectUnexpectedErrors(page);
  const questionId = await firstEnabledQuestionId(page);

  await page.goto('/settings');
  await expect(page.getByRole('heading', { name: '内容管理' })).toBeVisible();
  await expect(page.getByText('系统状态')).toBeVisible();
  await expect(page.getByText('UP', { exact: true })).toBeVisible();
  await expect(page.getByText('V16', { exact: true })).toBeVisible();

  await page.goto('/questions?keyword=HashMap');
  await expect(page.getByPlaceholder('搜索标题或一句话理解')).toHaveValue('HashMap');
  await expect(page.getByText(/HashMap/).first()).toBeVisible();

  await page.goto(`/questions/${questionId}`);
  const favorite = page.getByRole('button', { name: /收藏/ });
  await expect(favorite).toBeVisible();
  const beforeFavorite = await favorite.textContent();
  await favorite.click();
  await expect(favorite).not.toHaveText(beforeFavorite ?? '');

  await page.getByRole('button', { name: '开始练习' }).click();
  await page.getByLabel('我的回答').fill('P07 浏览器真实回答。');
  await page.getByRole('button', { name: '查看参考答案' }).click();
  await expect(page.getByText('参考答案', { exact: true })).toBeVisible();
  expect(errors).toEqual([]);
});

test('真实失败路径与幂等重试返回稳定错误契约', async ({ page }) => {
  const questionId = await firstEnabledQuestionId(page);
  const attemptId = randomUUID();
  const attempt = {
    questionId,
    clientAttemptId: attemptId,
    answerText: 'P07 重试语义验证',
    viewedAnswer: true,
    selfRating: 2,
    resultType: 'WRONG',
    elapsedMs: 500,
  };
  const first = await page.request.post('/api/v1/study/attempts', { data: attempt });
  expect(first.ok()).toBeTruthy();
  expect((await first.json() as { data: { duplicated: boolean } }).data.duplicated).toBe(false);
  const duplicate = await page.request.post('/api/v1/study/attempts', { data: attempt });
  expect(duplicate.ok()).toBeTruthy();
  expect((await duplicate.json() as { data: { duplicated: boolean } }).data.duplicated).toBe(true);

  const noteResponse = await page.request.get(`/api/v1/study/notes?targetType=QUESTION&targetId=${questionId}`);
  const currentNote = (await noteResponse.json() as { data: null | { id: number; version: number } }).data;
  const note = currentNote ?? (await (await page.request.post('/api/v1/study/notes', {
    data: { targetType: 'QUESTION', targetId: questionId, content: 'P07 冲突基线' },
  })).json() as { data: { id: number; version: number } }).data;
  const updated = await page.request.put(`/api/v1/study/notes/${note.id}`, { data: { content: 'P07 最新版本', version: note.version } });
  expect(updated.ok()).toBeTruthy();
  const stale = await page.request.put(`/api/v1/study/notes/${note.id}`, { data: { content: 'P07 过期覆盖', version: note.version } });
  expect(stale.status()).toBe(409);
  expect((await stale.json() as { code: string }).code).toBe('VERSION_CONFLICT');

  const invalidSeed = await page.request.post('/api/v1/system/seeds/validate', {
    multipart: { file: { name: 'invalid.json', mimeType: 'application/json', buffer: Buffer.from('{invalid', 'utf8') } },
  });
  expect(invalidSeed.status()).toBe(422);
  expect((await invalidSeed.json() as { code: string }).code).toBe('CONTENT_VALIDATION_FAILED');

  await page.goto('/questions/999999999');
  await expect(page.getByText('题目工作区加载失败')).toBeVisible();
});
