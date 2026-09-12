import { get, post, put, del } from './request.js';

/**
 * 获取当前用户宠物列表（含疫苗记录）
 */
export function getPetList() {
  return get('/pets/list');
}

/**
 * 获取宠物详情（含疫苗记录）
 * @param {number|string} petId
 */
export function getPetDetail(petId) {
  return get(`/pets/${petId}`);
}

/**
 * 添加宠物
 * @param {Object} data - { name, avatar, petType, breed, birthday, gender, weight, color, sterilization, remark, vaccineList }
 *                        petType：cat-猫 dog-狗 other-其他（用于首页养宠知识兴趣推荐）
 */
export function addPet(data) {
  return post('/pets', data);
}

/**
 * 更新宠物
 * @param {number|string} petId
 * @param {Object} data - { name, avatar, petType, breed, birthday, gender, weight, color, sterilization, remark, vaccineList }
 */
export function updatePet(petId, data) {
  return put(`/pets/${petId}`, data);
}

/**
 * 删除宠物
 * @param {number|string} petId
 */
export function deletePet(petId) {
  return del(`/pets/${petId}`);
}
