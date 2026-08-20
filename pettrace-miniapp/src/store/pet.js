import { defineStore } from 'pinia';
import { ref } from 'vue';
import { getPetList, getPetDetail } from '@/api/pet.js';

export const usePetStore = defineStore('pet', () => {
  // State
  const petList = ref([]);
  const currentPet = ref(null);
  const loading = ref(false);

  // Actions
  const setPetList = (list) => {
    petList.value = list;
  };

  const setCurrentPet = (pet) => {
    currentPet.value = pet;
  };

  const fetchPetList = async () => {
    loading.value = true;
    try {
      const res = await getPetList();
      // API 直接返回 data 数组
      petList.value = res.data || [];
      return res.data;
    } catch (err) {
      return Promise.reject(err);
    } finally {
      loading.value = false;
    }
  };

  const fetchPetDetail = async (id) => {
    loading.value = true;
    try {
      const res = await getPetDetail(id);
      currentPet.value = res.data;
      return res.data;
    } catch (err) {
      return Promise.reject(err);
    } finally {
      loading.value = false;
    }
  };

  return {
    petList,
    currentPet,
    loading,
    setPetList,
    setCurrentPet,
    fetchPetList,
    fetchPetDetail,
  };
});
