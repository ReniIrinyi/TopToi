<template>
  <div>
    <Header />
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
    />
    <ToiletCard   @travelMode="travelTo"  @navigate-to="panTo" v-if="selectedToilet" :toilet="selectedToilet" />
    <button v-if="!showToiletList && !showAddToilet" class="btn btn-wc" @click="showToiletList =true">🚻</button>
    <button v-if="!showAddToilet && !showToiletList" class="btn btn-add" @click="showAddToilet =true">+</button>
    <AddToilet v-if="showAddToilet" @close="showAddToilet = false" />
  </div>
</template>

<script>
import MapComponent from '../components/Map.vue';
import ToiletCard from '../components/ToiletCard.vue';
import ToiletList from '../components/ToiletList.vue';
import AddToilet from '../components/AddToilet.vue';
import apiService from '@/service/apiService.js';
import Header from '../components/Header.vue';

export default {
  components: { MapComponent, ToiletCard, ToiletList, AddToilet, Header },
  data() {
    return {
      selectedToilet: null,
      toilets: [],
      showToiletList: true,
      showAddToilet: false,
      userPosition: {lat: 47.292511253285674, lng: 7.959213904251256},
    };
  },
  computed: {
    sortedToilets() {
      return this.toilets?.sort((a, b) => {
        const distA = this.getDistance(a?.location || a?.geometry?.location);
        const distB = this.getDistance(b?.location || b?.geometry?.location);
        return distA - distB;
      });
    },
  },
  mounted() {
    navigator.geolocation.getCurrentPosition(
        pos => {
          this.userPosition = {
            lat: pos.coords?.latitude,
            lng: pos.coords?.longitude,
          };
          this.loadToilets();
        },
        () => {
          this.loadToilets();
        }
    );
  },
  methods: {
    travelTo(method){
      this.$refs.map.setTravelMode(method)
    },
    panTo(location) {
      this.$refs.map.drawRoute(
          this.userPosition,
          { lat: location.lat, lng: location.lng }
      );
    },
    loadToilets() {
      apiService.getToilets(this.userPosition?.lat, this.userPosition?.lng).then(res => {
        this.toilets = res.data.filter(t => t.id);
      });
    },
    selectToilet(toilet) {
      this.selectedToilet = toilet;
    },
    closeToiletList() {
      this.showToiletList = false;
    },
    getDistance(location) {
      const lat1 = this.userPosition?.lat;
      const lon1 = this.userPosition?.lng;
      const lat2 = location?.lat;
      const lon2 = location?.lng;

      const R = 6371;
      const dLat = ((lat2 - lat1) * Math.PI) / 180;
      const dLon = ((lon2 - lon1) * Math.PI) / 180;

      const a =
          Math.sin(dLat / 2) * Math.sin(dLat / 2) +
          Math.cos((lat1 * Math.PI) / 180) *
          Math.cos((lat2 * Math.PI) / 180) *
          Math.sin(dLon / 2) *
          Math.sin(dLon / 2);
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      return R * c;
    },
  },
};
</script>

<style scoped>
.btn {
  position: fixed;
  right: 20px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background-color: white;
  color: #008080;
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
