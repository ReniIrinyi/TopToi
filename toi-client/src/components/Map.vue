<template>
  <div id="map" :style="{ width: '100%', height: `${mapHeight}px` }"></div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue';

const props = defineProps({
  toilets: Array,
  center:Object
});

let directionsService;
let directionsRenderer;

const travelMode= ref(google.maps.TravelMode.WALKING)
const map = ref(null);
const mapHeight = ref(window.innerHeight);

function renderMarkers() {
  if (!map.value || !props.toilets) return;

  props.toilets.forEach(toilet => {
    const marker = new google.maps.Marker({
      map: map.value,
      position: {
        lat: toilet.location?.latitude ?? toilet.latitude,
        lng: toilet.location?.longitude ?? toilet.longitude,
      },
      title: toilet.name,
    });
  });
}

function initMap() {
  map.value = new google.maps.Map(document.getElementById("map"), {
    zoom: 16,
    center: props.center,
    disableDefaultUI: true,
    styles: [
      {
        elementType: "geometry",
        stylers: [{color: "#f5f5f5"}]
      },
      {
        elementType: "labels.icon",
        stylers: [{visibility: "off"}]
      },
      {
        elementType: "labels.text.fill",
        stylers: [{color: "#616161"}]
      },
      {
        elementType: "labels.text.stroke",
        stylers: [{color: "#f5f5f5"}]
      },
      {
        featureType: "administrative.land_parcel",
        elementType: "labels.text.fill",
        stylers: [{color: "#bdbdbd"}]
      },
      {
        featureType: "poi",
        elementType: "geometry",
        stylers: [{color: "#eeeeee"}]
      },
      {
        featureType: "poi",
        elementType: "labels.text.fill",
        stylers: [{color: "#757575"}]
      },
      {
        featureType: "poi.park",
        elementType: "geometry",
        stylers: [{color: "#e5e5e5"}]
      },
      {
        featureType: "poi.park",
        elementType: "labels.text.fill",
        stylers: [{color: "#9e9e9e"}]
      },
      {
        featureType: "road",
        elementType: "geometry",
        stylers: [{color: "#ffffff"}]
      },
      {
        featureType: "road.arterial",
        elementType: "labels.text.fill",
        stylers: [{color: "#757575"}]
      },
      {
        featureType: "road.highway",
        elementType: "geometry",
        stylers: [{color: "#dadada"}]
      },
      {
        featureType: "road.highway",
        elementType: "labels.text.fill",
        stylers: [{color: "#616161"}]
      },
      {
        featureType: "transit",
        elementType: "geometry",
        stylers: [{color: "#e5e5e5"}]
      },
      {
        featureType: "transit.station",
        elementType: "labels.text.fill",
        stylers: [{color: "#757575"}]
      },
      {
        featureType: "water",
        elementType: "geometry",
        stylers: [{color: "#c9c9c9"}]
      },
      {
        featureType: "water",
        elementType: "labels.text.fill",
        stylers: [{color: "#9e9e9e"}]
      }
    ]
  });

  renderMarkers();
}

onMounted(() => {
  if (!window.google || !window.google.maps) {
    window.initMap = initMap;
    const apiKey = 'AIzaSyCyiyQzvlmWxLZkMC-SrNriQ3BcUXFII_M'
    const script = document.createElement("script");
    script.src = `https://maps.googleapis.com/maps/api/js?key=${apiKey}&libraries=places&callback=initMap`;
    script.async = true;
    document.head.appendChild(script);
  } else {
    initMap();
  }
});


function setTravelMode(mode) {
  console.log(mode)
  travelMode.value = google.maps.TravelMode[mode];
}

function drawRoute(from, to) {
  directionsService = new google.maps.DirectionsService()
  directionsRenderer = new google.maps.DirectionsRenderer()
  directionsRenderer.setMap(map.value)

  directionsService.route(
      {
        origin: from,
        destination: to,
        travelMode: this.travelMode.value,
      },
      (result, status) => {
        if (status === "OK") {
          directionsRenderer.setDirections(result);
        } else {
          console.error("Útvonal nem található: " + status);
        }
      }
  );
}


defineExpose({
  drawRoute,
  setTravelMode
});


  watch(() => props.toilets, () => {
    renderMarkers();
});
</script>
