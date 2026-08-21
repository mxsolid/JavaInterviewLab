import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '.', '');
  return {
    plugins: [react()],
    build: {
      manifest: true,
      rolldownOptions: {
        output: {
          // 共享依赖按真实入口集合拆分，避免任一页面把整套 Ant Design 打进入口共享块。
          codeSplitting: {
            groups: [{
              name: 'vendor',
              test: /node_modules[\\/]/,
              priority: 10,
              entriesAware: true,
              maxSize: 400 * 1024,
              minSize: 20 * 1024,
            }],
          },
        },
      },
    },
    server: {
      port: 5173,
      proxy: {
        '/api': {
          target: env.VITE_BACKEND_URL || 'http://127.0.0.1:8080',
          changeOrigin: true,
        },
      },
    },
  };
});
