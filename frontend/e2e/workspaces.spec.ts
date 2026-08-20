import { mkdir } from 'node:fs/promises';
import { resolve } from 'node:path';
import { expect, test, type Page } from '@playwright/test';

const evidenceDirectory = resolve('..', 'docs', 'v03', 'validation', 'p06', 'screenshots');

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

async function capture(page: Page, route: string, heading: string, name: string, width: number, height: number) {
  await page.setViewportSize({ width, height });
  await page.emulateMedia({ reducedMotion: 'reduce' });
  await page.goto(route);
  await expect(page.getByRole('heading', { name, exact: false }).or(page.getByRole('heading', { name: heading, exact: false }))).toBeVisible();
  await page.screenshot({ path: resolve(evidenceDirectory, `${name}.png`), fullPage: false, animations: 'disabled' });
}

test('四个工作区生成桌面与移动端基线', async ({ page }) => {
  await mkdir(evidenceDirectory, { recursive: true });
  const errors = collectErrors(page);
  await capture(page, '/scenarios', '场景训练', 'scenario-1720', 1720, 1000);
  await capture(page, '/source', '源码 + 注释阅读', 'source-1720', 1720, 1000);
  await capture(page, '/lab', '动画实验室', 'lab-1720', 1720, 1000);
  await capture(page, '/interview', '模拟面试', 'interview-1720', 1720, 1000);
  await capture(page, '/scenarios', '场景训练', 'scenario-mobile-390', 390, 844);
  await capture(page, '/interview', '模拟面试', 'interview-mobile-390', 390, 844);
  expect(errors).toEqual([]);
});

test('场景练习真实写库并展示结果', async ({ page }) => {
  const errors = collectErrors(page);
  await page.goto('/scenarios');
  await page.getByLabel('场景回答').fill(`P06 场景验收 ${Date.now()}：先验证约束，再定位根因并给出恢复方案。`);
  await page.getByRole('button', { name: '提交场景练习' }).click();
  await expect(page.getByText('场景练习已保存')).toBeVisible();
  await expect(page.getByText('参考分析主线')).toBeVisible();
  expect(errors).toEqual([]);
});

test('源码行注释和关联题目入口可用', async ({ page }) => {
  const errors = collectErrors(page);
  await page.goto('/source');
  const annotatedLine = page.locator('.source-code-line.annotated').first();
  await expect(annotatedLine).toBeVisible();
  await annotatedLine.click();
  await expect(page.getByText(/当前片段共有 \d+ 条行级注释/)).toBeVisible();
  const related = page.getByRole('button', { name: '查看关联题目' });
  await expect(related).toBeEnabled();
  await related.click();
  await expect(page.getByRole('heading', { name: '题库' })).toBeVisible();
  expect(errors).toEqual([]);
});

test('五个纯状态机实验均可运行', async ({ page }) => {
  const errors = collectErrors(page);
  await page.goto('/lab');
  const selector = page.getByLabel('切换实验');
  const options = await selector.locator('option').count();
  expect(options).toBe(0);
  await selector.click();
  const labOptions = page.locator('.ant-select-dropdown:visible .ant-select-item-option');
  await expect(labOptions).toHaveCount(5);
  const labels = await labOptions.allTextContents();
  await page.keyboard.press('Escape');
  for (const label of labels) {
    await selector.click();
    await page.locator('.ant-select-dropdown:visible .ant-select-item-option').filter({ hasText: label }).click();
    await expect(page.getByLabel('实验状态')).toBeVisible();
    await page.getByRole('button', { name: '下一步' }).click();
    await expect(page.getByText(/Step 2 \/ /)).toBeVisible();
  }
  expect(errors).toEqual([]);
});

test('文本面试返回四维可解释评分并完成会话', async ({ page }) => {
  const errors = collectErrors(page);
  await page.goto('/interview');
  await page.getByRole('button', { name: '开始模拟面试' }).click();
  await expect(page.getByText('Java 面试官')).toBeVisible();
  await page.getByLabel('面试回答').fill('核心结论是先保证线程安全。因为共享状态存在竞态，所以使用并发容器或锁保护不变量。边界是锁粒度会影响吞吐量，例如高并发缓存更新需要控制临界区。');
  await page.getByRole('button', { name: '提交并评分' }).click();
  await expect(page.getByText('可解释评分')).toBeVisible();
  await expect(page.locator('.score-card')).toHaveCount(4);
  await page.getByRole('button', { name: '结束并汇总' }).click();
  await expect(page.getByText(/面试已结束/)).toBeVisible();
  expect(errors).toEqual([]);
});
