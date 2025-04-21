<template>
  <div class="home-container">
    <ToiletList
        v-if="showToiletList && !selectedToilet"
        :userPosition="userPosition"
        :toilets="sortedToilets"
        @selected-toilet="selectToilet"
        @close="closeToiletList"
    />
    <MapComponent
        v-else
        :toilets="sortedToilets"
        @selected-toilet="selectToilet"
        :center="userPosition"
        ref="map"
        @map-moved="onMapMoved"
        @onAuthChange="loadToilets"

    />
    <ToiletCard @close="loadToilets"  @travelMode="travelTo"  @navigate-to="panTo" v-if="selectedToilet" :toilet="selectedToilet" />
    <button v-if="!showToiletList && !showAddToilet" class="btn btn-wc" @click="showToiletList =true">🚻</button>
    <button v-if="!showAddToilet && !showToiletList" class="btn btn-add" @click="showAddToilet =true">+</button>
    <AddToilet v-if="showAddToilet" @close="showAddToilet = false; loadToilets" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import MapComponent from './Map.vue';
import ToiletCard from './ToiletCard.vue';
import ToiletList from './ToiletList.vue';
import AddToilet from './AddToilet.vue';
import apiService from '@/service/apiService.js';

const selectedToilet = ref(null);
const toilets = ref([]);
const showToiletList = ref(false);
const showAddToilet = ref(false);
const lastFetchedPosition = ref(null);
const userPosition = ref({ lat: null, lng: null });

const map = ref(null);

const sortedToilets = computed(() => {
  return toilets.value?.sort((a, b) => {
    const locA = a?.location || a?.geometry?.location;
    const locB = b?.location || b?.geometry?.location;

    if (!isValidCoordinate(locA) || !isValidCoordinate(locB)) return 0;

    const distA = getDistance(userPosition.value, locA);
    const distB = getDistance(userPosition.value, locB);
    return distA - distB;
  });
});

onMounted(() => {
  navigator.geolocation.watchPosition(
      (pos) => {
        const newPos = {
          lat: pos.coords.latitude,
          lng: pos.coords.longitude
        };

        userPosition.value = newPos;

        if (!lastFetchedPosition.value) {
          lastFetchedPosition.value = { ...newPos };
          loadToilets();
          return;
        }

        const dist = getDistance(lastFetchedPosition.value, newPos);

        if (dist > 1) {
          loadToilets();
          lastFetchedPosition.value = { ...newPos };
        }
      },
      err => {
        console.error(err);
      },
      {
        enableHighAccuracy: true,
        timeout: 5000,
        maximumAge: 0
      }
  );
  if(toilets.value.length>0){
    showToiletList.value = true
  } else {
    showToiletList.value = false
  }
});

function isValidCoordinate(loc) {
  return loc &&
      typeof loc.lat === 'number' &&
      typeof loc.lng === 'number' &&
      isFinite(loc.lat) &&
      isFinite(loc.lng);
}

function onMapMoved(center) {
  const dist = lastFetchedPosition.value
      ? getDistance(lastFetchedPosition.value, center)
      : Infinity;

  if (dist > 1) {
    userPosition.value = center;
    lastFetchedPosition.value = center;
    loadToilets();
  }
}

function travelTo(method) {
  map.value?.setTravelMode(method);
}

function panTo(location) {
  map.value?.drawRoute(userPosition.value, {
    lat: location.lat,
    lng: location.lng
  });
}

function loadToilets() {
  console.log('get toilets...');
  apiService.getToilets(userPosition.value?.lat, userPosition.value?.lng).then(res => {
    console.log(res);
    toilets.value = res.data.filter(t => t.id);
  });
}

function selectToilet(toilet) {
  selectedToilet.value = toilet;
}

function closeToiletList() {
  showToiletList.value = false;
}

function getDistance(pos1, pos2) {
  const R = 6371;
  const toRad = x => (x * Math.PI) / 180;

  const dLat = toRad(pos2.lat - pos1.lat);
  const dLon = toRad(pos2.lng - pos1.lng);

  const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(toRad(pos1.lat)) * Math.cos(toRad(pos2.lat)) *
      Math.sin(dLon / 2) * Math.sin(dLon / 2);

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}
</script>

<style scoped>

.home-container{
  height: 100vh;
}

.btn {
  position: fixed;
  right: 20px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background-color: var(--primary-background);
  color: var(--primary-color);
  font-size: 32px;
  font-weight: bold;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  z-index: 1000;
}

.btn-add{
  bottom: 90px;
}

.btn-wc{
  bottom: 20px;
}

</style>
