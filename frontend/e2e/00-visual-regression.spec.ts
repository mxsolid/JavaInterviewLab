import { expect, test, type Page } from '@playwright/test';

interface VisualCase {
  route: string;
  heading: string;
  snapshot: string;
  width: number;
  height: number;
}

async function firstEnabledQuestionId(page: Page): Promise<number> {
  const response = await page.request.get('/api/questions?page=1&pageSize=1&status=ENABLED');
  expect(response.ok()).toBeTruthy();
  const body = await response.json() as { data: { items: Array<{ id: number }> } };
  return body.data.items[0].id;
}

async function verifyVisual(page: Page, visualCase: VisualCase) {
  await page.setViewportSize({ width: visualCase.width, height: visualCase.height });
  await page.emulateMedia({ reducedMotion: 'reduce' });
  await page.goto(visualCase.route);
  await expect(page.getByRole('heading', { name: visualCase.heading })).toBeVisible();
  // 同一 Page 连续访问长页面时浏览器可能恢复旧滚动位置，视觉基线必须固定在页面顶部。
  await page.waitForTimeout(100);
  await page.evaluate(() => {
    document.documentElement.style.scrollBehavior = 'auto';
    window.scrollTo(0, 0);
    document.querySelectorAll<HTMLElement>('*').forEach((element) => {
      if (element.scrollTop > 0) element.scrollTop = 0;
      if (element.scrollLeft > 0) element.scrollLeft = 0;
    });
  });
  await expect.poll(() => page.evaluate(() => window.scrollY)).toBe(0);
  await expect.poll(() => page.evaluate(() => [...document.querySelectorAll<HTMLElement>('*')]
    .filter((element) => element.scrollTop > 0 || element.scrollLeft > 0)
    .map((element) => element.className))).toEqual([]);
  await expect(page.locator('.app-topbar')).toBeInViewport();
  if (visualCase.width > 620) {
    await expect(page.locator('.brand')).toBeInViewport();
  } else {
    await expect(page.locator('.brand')).toBeHidden();
  }
  const topbarBox = await page.locator('.app-topbar').boundingBox();
  expect(topbarBox?.y).toBe(0);
  await expect(page).toHaveScreenshot(visualCase.snapshot, {
    animations: 'disabled',
    caret: 'hide',
    fullPage: false,
    maxDiffPixelRatio: 0.001,
  });
}

test('P08 规定视口与核心页面保持像素基线', async ({ page }) => {
  const questionId = await firstEnabledQuestionId(page);
  const cases: VisualCase[] = [
    { route: '/', heading: '首页 / 工作台', snapshot: 'workbench-1440.png', width: 1440, height: 1000 },
    { route: '/', heading: '首页 / 工作台', snapshot: 'workbench-1720.png', width: 1720, height: 1100 },
    { route: '/knowledge', heading: '知识地图', snapshot: 'knowledge-1720.png', width: 1720, height: 1100 },
    { route: `/questions/${questionId}`, heading: '题目学习', snapshot: 'question-1720.png', width: 1720, height: 1100 },
    { route: '/scenarios', heading: '场景训练', snapshot: 'scenario-1720.png', width: 1720, height: 1100 },
    { route: '/source', heading: '源码 + 注释阅读', snapshot: 'source-1720.png', width: 1720, height: 1100 },
    { route: '/lab', heading: '动画实验室', snapshot: 'lab-1720.png', width: 1720, height: 1100 },
    { route: '/review', heading: '复习中心', snapshot: 'review-1720.png', width: 1720, height: 1100 },
    { route: '/', heading: '首页 / 工作台', snapshot: 'workbench-1920.png', width: 1920, height: 1080 },
    { route: '/', heading: '首页 / 工作台', snapshot: 'workbench-mobile-390.png', width: 390, height: 844 },
    { route: `/questions/${questionId}`, heading: '题目学习', snapshot: 'question-mobile-390.png', width: 390, height: 844 },
  ];

  for (const visualCase of cases) await verifyVisual(page, visualCase);
});

test('首页不请求 Lab 与 Source 路由模块', async ({ page }) => {
  const scripts: string[] = [];
  page.on('request', (request) => {
    if (request.resourceType() === 'script') scripts.push(request.url());
  });
  await page.goto('/');
  await expect(page.getByRole('heading', { name: '首页 / 工作台' })).toBeVisible();
  expect(scripts.filter((url) => /features\/(lab|source)\//i.test(url))).toEqual([]);
});

test('键盘、可访问名称、状态文本和 reduced motion 可用', async ({ page }) => {
  await page.emulateMedia({ reducedMotion: 'reduce' });
  await page.goto('/');
  await expect(page.getByRole('heading', { name: '首页 / 工作台' })).toBeVisible();
  expect(await page.evaluate(() => matchMedia('(prefers-reduced-motion: reduce)').matches)).toBe(true);

  const namelessButtons = await page.getByRole('button').evaluateAll((buttons) => buttons
    .filter((button) => !(button.getAttribute('aria-label')
      || button.getAttribute('title')
      || button.textContent?.trim()))
    .map((button) => button.outerHTML));
  expect(namelessButtons).toEqual([]);

  const focusTrail: Array<{ tag: string; name: string; visibleIndicator: boolean }> = [];
  for (let index = 0; index < 16; index += 1) {
    await page.keyboard.press('Tab');
    focusTrail.push(await page.evaluate(() => {
      const active = document.activeElement as HTMLElement;
      const style = getComputedStyle(active);
      return {
        tag: active.tagName,
        name: active.getAttribute('aria-label') || active.innerText || active.getAttribute('href') || '',
        visibleIndicator: style.outlineStyle !== 'none' || style.boxShadow !== 'none',
      };
    }));
  }
  expect(focusTrail.some((item) => item.tag === 'A')).toBe(true);
  expect(focusTrail.some((item) => item.tag === 'BUTTON' || item.tag === 'INPUT')).toBe(true);
  expect(focusTrail.some((item) => item.visibleIndicator)).toBe(true);

  await page.goto('/knowledge');
  await expect(page.getByText(/未开始|学习中|已掌握/).first()).toBeVisible();
});
