import { defineConfig, devices } from '@playwright/test';

const manageBackend = process.env.PLAYWRIGHT_MANAGED_BACKEND !== 'false';

export default defineConfig({
  testDir: './e2e',
  outputDir: './test-results',
  timeout: 30_000,
  workers: 1,
  expect: { timeout: 5_000 },
  use: {
    baseURL: 'http://127.0.0.1:5173',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'], channel: 'chrome' } },
  ],
  // E2E 使用独立学习档案并串行执行，避免本地历史数据和并发写入造成视觉漂移。
  webServer: [
    ...(manageBackend ? [{
      command: 'D:\\Tools\\PowerShell\\7\\pwsh.exe -NoProfile -File ..\\scripts\\07_e2e_backend.ps1',
      url: 'http://127.0.0.1:8080/actuator/health',
      reuseExistingServer: false,
      timeout: 120_000,
    }] : []),
    {
      command: 'npm run dev -- --host 127.0.0.1',
      url: 'http://127.0.0.1:5173',
      reuseExistingServer: false,
      timeout: 30_000,
    },
  ],
});
