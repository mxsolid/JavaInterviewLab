import { mkdir } from 'node:fs/promises';
import { resolve } from 'node:path';
import { expect, test } from '@playwright/test';
import { captureViewport } from './helpers/screenshot';

const evidenceDirectory = resolve('..', 'docs', 'v03', 'validation', 'p04', 'screenshots');

test('canonical shell 在桌面和移动端保持可用', async ({ page }) => {
  await mkdir(evidenceDirectory, { recursive: true });
  const errors: string[] = [];
  page.on('console', (message) => {
    if (message.type() === 'error') errors.push(message.text());
  });
  page.on('pageerror', (error) => errors.push(error.message));
  page.on('response', (response) => {
    if (response.status() >= 400) errors.push(`${response.status()} ${response.url()}`);
  });

  for (const viewport of [
    { name: 'shell-1440', width: 1440, height: 900 },
    { name: 'shell-1720', width: 1720, height: 1000 },
    { name: 'shell-1920', width: 1920, height: 1080 },
    { name: 'shell-mobile-390', width: 390, height: 844 },
  ]) {
    await captureViewport(page, viewport, resolve(evidenceDirectory, `${viewport.name}.png`));
  }

  await expect(page.getByRole('navigation', { name: '主导航' })).toBeVisible();
  expect(errors).toEqual([]);
});

test('全局搜索支持键盘、刷新和真实题库参数', async ({ page }) => {
  await page.goto('/');
  const search = page.getByLabel('全局搜索');
  await search.focus();
  await expect(search).toBeFocused();
  await search.fill('HashMap');
  await search.press('Enter');
  await expect(page).toHaveURL(/\/questions\?keyword=HashMap$/);
  await expect(page.getByRole('heading', { name: '题库' })).toBeVisible();
  await page.reload();
  await expect(page.getByRole('heading', { name: '题库' })).toBeVisible();
  await expect(page.getByPlaceholder('搜索标题或一句话理解')).toHaveValue('HashMap');
});

test('全部 shell 导航入口无 console 与网络错误', async ({ page }) => {
  const errors: string[] = [];
  page.on('console', (message) => {
    if (message.type() === 'error') errors.push(message.text());
  });
  page.on('pageerror', (error) => errors.push(error.message));
  page.on('response', (response) => {
    if (response.status() >= 400) errors.push(`${response.status()} ${response.url()}`);
  });

  for (const [route, heading] of [
    ['/', '首页 / 工作台'],
    ['/knowledge', '知识地图'],
    ['/questions', '题库'],
    ['/scenarios', '场景训练'],
    ['/review', '复习中心'],
    ['/source', '源码阅读'],
    ['/lab', '动画实验室'],
    ['/interview', '模拟面试'],
    ['/ai', 'AI 专题'],
    ['/settings', '内容管理'],
  ]) {
    await page.goto(route);
    await expect(page.getByRole('heading', { name: heading })).toBeVisible();
  }

  expect(errors).toEqual([]);
});

test('基础 Loading、Empty、Error 状态可见', async ({ page }) => {
  await page.route('**/api/dashboard', async (route) => {
    await new Promise((resolve) => setTimeout(resolve, 500));
    await route.fulfill({ status: 500, contentType: 'application/json', body: '{"success":false}' });
  });
  await page.goto('/');
  await expect(page.getByLabel('内容加载中')).toBeVisible();
  await expect(page.getByText('学习看板加载失败')).toBeVisible();
  await page.goto('/knowledge');
  await expect(page.getByText('当前工作区正在接入真实 API')).toBeVisible();
});
