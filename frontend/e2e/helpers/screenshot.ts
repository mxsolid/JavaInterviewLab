import type { Page } from '@playwright/test';

export interface ScreenshotViewport {
  name: string;
  width: number;
  height: number;
}

/** 固定视口和动画状态，避免同一页面因环境差异产生无意义像素漂移。 */
export async function captureViewport(
  page: Page,
  viewport: ScreenshotViewport,
  outputPath: string,
) {
  await page.setViewportSize({ width: viewport.width, height: viewport.height });
  await page.emulateMedia({ reducedMotion: 'reduce' });
  await page.goto('/');
  await page.getByRole('heading', { name: '首页' }).waitFor();
  await page.screenshot({ path: outputPath, fullPage: false, animations: 'disabled' });
}
