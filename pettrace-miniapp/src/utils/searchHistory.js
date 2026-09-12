/**
 * 养宠知识 · 搜索历史（本地缓存）
 *
 * 说明：
 *   - 搜索记录只存在用户手机本地（uni.setStorageSync），不上报服务端。
 *   - 最多保留 MAX_HISTORY 条，重复关键词自动提到最前。
 *   - 用户可以在搜索页删除单条、一键清空，或选择"不显示"关闭历史记录展示；
 *     关闭是本地偏好（VISIBLE_KEY），关闭后不再读取展示，可随时重新开启。
 */

/** 搜索历史缓存key */
const HISTORY_KEY = 'article_search_history';

/** 是否展示历史的偏好key */
const VISIBLE_KEY = 'article_search_history_visible';

/** 最多保留的搜索记录条数 */
const MAX_HISTORY = 20;

/**
 * 读取搜索历史
 * @returns {string[]} 关键词数组（最近搜索在最前）
 */
export function getSearchHistory() {
  try {
    const list = uni.getStorageSync(HISTORY_KEY);
    if (!Array.isArray(list)) return [];
    return list.filter((item) => typeof item === 'string' && item.trim());
  } catch (e) {
    return [];
  }
}

/**
 * 写入搜索历史（内部方法）
 * @param {string[]} list
 */
function saveSearchHistory(list) {
  try {
    uni.setStorageSync(HISTORY_KEY, list.slice(0, MAX_HISTORY));
  } catch (e) {
    console.error('[搜索历史] 写入失败:', e);
  }
}

/**
 * 记录一次搜索
 *
 * 已存在的关键词会被移到最前，并去除首尾空格；
 * 空字符串不记录。
 *
 * @param {string} keyword 搜索关键词
 * @returns {string[]} 更新后的历史列表
 */
export function addSearchHistory(keyword) {
  const word = (keyword || '').trim();
  if (!word) return getSearchHistory();

  const list = getSearchHistory().filter((item) => item !== word);
  list.unshift(word);
  saveSearchHistory(list);
  return list.slice(0, MAX_HISTORY);
}

/**
 * 删除单条搜索记录
 * @param {string} keyword 关键词
 * @returns {string[]} 更新后的历史列表
 */
export function removeSearchHistory(keyword) {
  const word = (keyword || '').trim();
  const list = getSearchHistory().filter((item) => item !== word);
  saveSearchHistory(list);
  return list;
}

/**
 * 清空全部搜索记录
 */
export function clearSearchHistory() {
  try {
    uni.removeStorageSync(HISTORY_KEY);
  } catch (e) {
    console.error('[搜索历史] 清空失败:', e);
  }
}

/**
 * 是否展示搜索历史（用户可在搜索页选择"不显示"）
 * @returns {boolean} 默认展示
 */
export function isHistoryVisible() {
  try {
    const visible = uni.getStorageSync(VISIBLE_KEY);
    // 无记录时默认展示
    return visible === '' || visible === undefined || visible === null ? true : !!visible;
  } catch (e) {
    return true;
  }
}

/**
 * 设置是否展示搜索历史
 * @param {boolean} visible
 */
export function setHistoryVisible(visible) {
  try {
    uni.setStorageSync(VISIBLE_KEY, !!visible);
  } catch (e) {
    console.error('[搜索历史] 保存展示偏好失败:', e);
  }
}
