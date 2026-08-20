import type { ThemeConfig } from 'antd';

export const appTheme: ThemeConfig = {
  token: {
    colorPrimary: '#5b6cf8',
    colorInfo: '#5b6cf8',
    colorSuccess: '#14b8a6',
    colorText: '#1f2937',
    colorBorder: '#e8ecf7',
    borderRadius: 16,
    fontSize: 14,
    controlHeight: 40,
    boxShadowSecondary: '0 12px 32px rgba(61, 80, 140, 0.08)',
  },
  components: {
    Button: { borderRadius: 10, fontWeight: 600 },
    Menu: { itemBorderRadius: 10, itemSelectedBg: '#eef1ff', itemSelectedColor: '#4d60e8' },
  },
};
