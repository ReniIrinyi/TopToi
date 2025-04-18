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
        @map-moved="onMapMoved"

    />
    <ToiletCard   @travelMode="travelTo"  @navigate-to="panTo" v-if="selectedToilet" :toilet="selectedToilet" />
    <button v-if="!showToiletList && !showAddToilet" class="btn btn-wc" @click="showToiletList =true">🚻</button>
    <button v-if="!showAddToilet && !showToiletList" class="btn btn-add" @click="showAddToilet =true">+</button>
    <AddToilet v-if="showAddToilet" @close="showAddToilet = false" />
  </div>
</template>

<script>
import MapComponent from './Map.vue';
import ToiletCard from './ToiletCard.vue';
import ToiletList from './ToiletList.vue';
import AddToilet from './AddToilet.vue';
import apiService from '@/service/apiService.js';
import Header from './Header.vue';

export default {
  components: { MapComponent, ToiletCard, ToiletList, AddToilet, Header },
  data() {
    return {
      selectedToilet: null,
      toilets: [],
      showToiletList: true,
      showAddToilet: false,
      lastFetchedPosition: null,
      userPosition: {lat:null, lng:null}
    };
  },
  computed: {
    sortedToilets() {
        return this.toilets?.sort((a, b) => {
          const locA = a?.location || a?.geometry?.location;
          const locB = b?.location || b?.geometry?.location;

          if (!this.isValidCoordinate(locA) || !this.isValidCoordinate(locB)) return 0;

          const distA = this.getDistance(this.userPosition, locA);
          const distB = this.getDistance(this.userPosition, locB);
          return distA - distB;
        });
      }
  },
  mounted() {
    navigator.geolocation.watchPosition(
        (pos) => {
          const newPos = {
            lat: pos.coords.latitude,
            lng: pos.coords.longitude
          };

          this.userPosition = newPos;

          if (!this.lastFetchedPosition) {
            this.lastFetchedPosition = { ...newPos };
            this.loadToilets();
            return;
          }

          const dist = this.getDistance(this.lastFetchedPosition, newPos);

          if (dist > 1) {
            this.loadToilets();
            this.lastFetchedPosition = { ...newPos };
          }
        },
        err => {
          console.error(err);
          // Fallback
          /*if (!this.userPosition) {
            this.userPosition = { lat: 47.2925, lng: 7.9592 };
            this.loadToilets();
          }*/
        },
        {
          enableHighAccuracy: true,
          timeout: 5000,
          maximumAge: 0
        }
    );
  },
  methods: {
    isValidCoordinate(loc) {
      return loc &&
          typeof loc.lat === 'number' &&
          typeof loc.lng === 'number' &&
          isFinite(loc.lat) &&
          isFinite(loc.lng);
    },
    onMapMoved(center) {
      const dist = this.lastFetchedPosition
          ? this.getDistance(this.lastFetchedPosition, center)
          : Infinity;

      if (dist > 1) {
        this.userPosition = center;
        this.lastFetchedPosition = center;
        this.loadToilets();
      }
    },
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
      console.log('get toalets...')
      apiService.getToilets(this.userPosition?.lat, this.userPosition?.lng).then(res => {
        console.log(res)
        this.toilets = res.data.filter(t => t.id);
      });
    },
    selectToilet(toilet) {
      this.selectedToilet = toilet;
    },
    closeToiletList() {
      this.showToiletList = false;
    },
    getDistance(pos1, pos2) {
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
