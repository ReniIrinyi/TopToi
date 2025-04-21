<template>
  <div class="nearby-list">
    <div class="header">
      <h2>{{ t('LABEL_NEAR_TOILETS') }}</h2>
      <button class="close-btn" @click="emit('close')">✖</button>
    </div>
    <ul>
      <li v-for="toilet in nearbyToilets" :key="toilet.id" @click="emit('selected-toilet', toilet)">
        <div class="toilet-name">{{ toilet.name }}</div>
        <div class="distance">{{ toilet.distance }} m</div>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { translate } from "@/service/translationService.js";

const props = defineProps({
  userPosition: Object,
  toilets: Array
});

const emit = defineEmits(['close', 'selected-toilet']);
const t = translate;

function calculateDistance(lat, lng) {
  const R = 6371;
  const dLat = (lat - props.userPosition.lat) * Math.PI / 180;
  const dLng = (lng - props.userPosition.lng) * Math.PI / 180;
  const a =
      Math.sin(dLat / 2) ** 2 +
      Math.cos(props.userPosition.lat * Math.PI / 180) *
      Math.cos(lat * Math.PI / 180) *
      Math.sin(dLng / 2) ** 2;
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return Math.round(R * c * 1000);
}

const nearbyToilets = computed(() => {
  return props.toilets
      .map(toilet => ({
        ...toilet,
        distance: calculateDistance(toilet.latitude, toilet.longitude)
      }))
      .sort((a, b) => a.distance - b.distance)
      .slice(0, 10);
});
</script>

<style scoped>
.nearby-list {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  background: var(--primary-background);
  z-index: 2000;
  padding: 20px;
  overflow-y: auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--primary-color);
  color: var(--primary-background);
  padding: 10px;
  border-radius: 6px;
}

ul {
  list-style: none;
  padding: 0;
  margin-top: 20px;
}

li {
  background: var(--primary-color);
  border-radius: 6px;
  color: var(--primary-background);
  margin-bottom: 12px;
  padding: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  font-weight: bold;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
</style>
