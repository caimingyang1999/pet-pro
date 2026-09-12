/**
 * 宠物类型词表
 *
 * 与后端 `pet_article.pet_type` / `pet_info.pet_type` 保持同一套七类词表
 * （项目里 `ai_suggest_word.pet_type` 用的也是这一套，不要另起炉灶）：
 *
 *   general-通用 / cat-猫 / dog-狗 / rabbit-兔子 / bird-鸟 / fish-鱼 / other-其他
 *
 * - 文章 `pet_article.pet_type` 七类都能用，general（或 NULL）表示"所有宠物都适用"。
 * - 宠物 `pet_info.pet_type` 没有"通用"概念，只取后六类；NULL 视为 other。
 */

/** 宠物类型选项（宠物档案录入用，不含"通用"） */
export const PET_TYPE_OPTIONS = [
  { value: 'cat', label: '猫' },
  { value: 'dog', label: '狗' },
  { value: 'rabbit', label: '兔子' },
  { value: 'bird', label: '鸟' },
  { value: 'fish', label: '鱼' },
  { value: 'other', label: '其他' },
];

/** 文章「适用宠物」选项（比宠物多一个"通用"） */
export const ARTICLE_PET_TYPE_OPTIONS = [
  { value: 'general', label: '通用' },
  ...PET_TYPE_OPTIONS,
];

/**
 * 宠物类型对应的兜底头像 emoji
 *
 * 取值与宠物详情页「按品种关键词兜底」的那套保持一致（鱼用 🐠 而非 🐟）。
 * 注意：详情页对 `other` 不直接采用本表的 emoji，而是继续按品种关键词猜。
 */
export const PET_TYPE_EMOJI = {
  cat: '🐱',
  dog: '🐶',
  rabbit: '🐰',
  bird: '🐦',
  fish: '🐠',
  other: '🐾',
  general: '🐾',
};

/** 可参与"兴趣优先"推荐的类型：通用的 general 与未分类的 other 不参与优先 */
const RECOMMENDABLE_TYPES = ['cat', 'dog', 'rabbit', 'bird', 'fish'];

/**
 * 判断宠物类型是否可参与兴趣优先推荐
 * @param {string} petType 宠物类型
 * @returns {boolean}
 */
export function isRecommendablePetType(petType) {
  return RECOMMENDABLE_TYPES.includes(petType);
}

const LABEL_MAP = {
  general: '通用',
  cat: '猫',
  dog: '狗',
  rabbit: '兔子',
  bird: '鸟',
  fish: '鱼',
  other: '其他',
};

/**
 * 取宠物类型的中文文案
 * @param {string} petType  宠物类型
 * @param {string} fallback 无对应值时的兜底文案（默认空串）
 * @returns {string}
 */
export function petTypeLabel(petType, fallback = '') {
  return LABEL_MAP[petType] || fallback;
}
