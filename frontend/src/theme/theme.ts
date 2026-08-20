import type { ThemeConfig } from 'antd';

export const appTheme: ThemeConfig = {
  token: {
    colorPrimary: '#0284c7',
    colorInfo: '#3b82f6',
    colorSuccess: '#10b981',
    colorWarning: '#f59e0b',
    colorError: '#f43f5e',
    colorText: '#1e293b',
    colorTextSecondary: '#64748b',
    colorBorder: '#e2e8f0',
    colorBgLayout: '#f0f4f9',
    borderRadius: 12,
    fontSize: 14,
    controlHeight: 40,
    boxShadowSecondary: '0 4px 20px -2px rgba(0, 0, 0, 0.05), 0 2px 6px -1px rgba(0, 0, 0, 0.02)',
  },
  components: {
    Button: { borderRadius: 10, fontWeight: 600 },
    Card: { borderRadiusLG: 16 },
    Input: { borderRadius: 11 },
    Menu: { itemBorderRadius: 11, itemSelectedBg: '#eaf2ff', itemSelectedColor: '#0369a1' },
  },
};
