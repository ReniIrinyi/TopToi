<template>
  <div class="nearby-list">
    <div class="header">
      <h2>Közeli mosdók</h2>
      <button class="close-btn" @click="$emit('close')">✖</button>
    </div>
    <ul>
      <li v-for="toilet in nearbyToilets" :key="toilet.id" @click="$emit('selected-toilet', toilet)">
        <div class="toilet-name">{{ toilet.name }}</div>
        <div class="distance">{{ toilet.distance }} m</div>
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  props: ['userPosition', 'toilets'],
  computed: {
    nearbyToilets() {
      return this.toilets.map(toilet => {
        const distance = this.calculateDistance(toilet.latitude, toilet.longitude);
        return { ...toilet, distance };
      }).sort((a, b) => a.distance - b.distance).slice(0, 10);
    },
  },
  methods: {
    calculateDistance(lat, lng) {
      const R = 6371;
      const dLat = (lat - this.userPosition.lat) * Math.PI / 180;
      const dLng = (lng - this.userPosition.lng) * Math.PI / 180;
      const a =
          Math.sin(dLat / 2) ** 2 +
          Math.cos(this.userPosition.lat * Math.PI / 180) *
          Math.cos(lat * Math.PI / 180) *
          Math.sin(dLng / 2) ** 2;
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      return Math.round(R * c * 1000);
    },
  },
};
</script>

<style scoped>
.nearby-list {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  background: white;
  z-index: 2000;
  padding: 20px;
  overflow-y: auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #008080;
  color: white;
  padding: 10px;
  border-radius: 6px;
}

.close-btn {
  background: none;
  color: white;
  font-size: 20px;
  border: none;
  cursor: pointer;
}

ul {
  list-style: none;
  padding: 0;
  margin-top: 20px;
}

li {
  background: #008080;
  border-radius: 6px;
  color: white;
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
