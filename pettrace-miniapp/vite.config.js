import { defineConfig, loadEnv } from 'vite';
import uni from '@dcloudio/vite-plugin-uni';
import path from 'path';

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  // 加载单一 .env 文件（不区分 mode，第三个参数传空字符串表示加载全部变量）
  const env = loadEnv('', process.cwd(), '');
  // 根据当前构建 mode 选择使用开发 / 生产环境地址
  const isProd = mode === 'production';
  const serverBase = isProd
    ? (env.VITE_PROD_SERVER_BASE || 'http://localhost:8080')
    : (env.VITE_DEV_SERVER_BASE || 'http://localhost:8080');

  return {
    plugins: [uni()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src'),
        'uview-plus': path.resolve(__dirname, 'node_modules/uview-plus'),
      },
    },
    css: {
      preprocessorOptions: {
        scss: {
          additionalData: `@import "uview-plus/theme.scss";`,
          silenceDeprecations: ['legacy-js-api', 'import'],  // 静默弃用警告
          quietDeps: true,  // 屏蔽依赖中的警告
        },
      },
    },
    server: {
      port: 3000,
      proxy: {
        '/api': {
          target: serverBase,
          changeOrigin: true,
        },
      },
    },
  };
});
