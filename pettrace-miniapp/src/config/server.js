/**
 * 服务器地址配置（单一来源）
 *
 * 独立成文件的原因：api/request.js 需要引用 utils/session.js 做登录态恢复，
 * 而 session.js 在静默重登时又需要 BASE_URL。若地址常量定义在 request.js 里，
 * 两个模块会形成循环依赖，在部分构建条件下会取到未初始化的值（TDZ）。
 * 因此把地址计算下沉到这里，request.js 再 re-export，保持既有 import 路径可用。
 *
 * 通过根目录 .env 文件配置（VITE_DEV_SERVER_BASE / VITE_PROD_SERVER_BASE + VITE_API_PREFIX）
 * 真机调试时把 VITE_DEV_SERVER_BASE 改为电脑的局域网 IP，例如 http://192.168.1.6:8080
 * （localhost 在手机上指向手机自己，会导致 ERR_CONNECTION_REFUSED）
 */
const {
  VITE_DEV_SERVER_BASE,
  VITE_PROD_SERVER_BASE,
  VITE_API_PREFIX,
  PROD,
} = import.meta.env;

// 服务器根地址，用于非 /api/v1 前缀的接口（如文件上传 /system/user/profile/avatar、/common/upload 等）
export const SERVER_BASE = PROD
  ? (VITE_PROD_SERVER_BASE || 'http://192.168.1.6:8080')
  : (VITE_DEV_SERVER_BASE || 'http://192.168.1.6:8080');

// API 接口完整地址 = 服务器根地址 + 接口前缀
export const BASE_URL = `${SERVER_BASE}${VITE_API_PREFIX || '/api/v1'}`;

export default { SERVER_BASE, BASE_URL };
