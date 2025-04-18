<template>
  <div style="width: 100%; height: 100vh;">
    <div id="header" style="height: 70px;">
      <Header />
    </div>
    <div id="map" style="width: 100%; height: calc(100vh - 70px);">
    </div>
  </div>
</template>


<script setup>
import { onMounted, ref, watch } from 'vue';
import Header from "@/views/Header.vue";
const emit = defineEmits(['map-moved']);

const props = defineProps({
  toilets: Array,
  center:Object
});

let directionsService;
let directionsRenderer;

const markers = [];
const travelMode= ref(null)
const map = ref(null);

function renderMarkers() {
  if (!map.value || !props.toilets) return;

  markers.forEach(marker => marker.setMap(null));
  markers.length = 0;

  props.toilets.forEach(toilet => {
    const lat = toilet.location?.latitude ?? toilet.latitude;
    const lng = toilet.location?.longitude ?? toilet.longitude;

    if (typeof lat !== 'number' || typeof lng !== 'number' || !isFinite(lat) || !isFinite(lng)) {
      return;
    }

    const marker = new google.maps.Marker({
      map: map.value,
      position: { lat, lng },
      title: toilet.name,
    });
    markers.push(marker);
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


  google.maps.event.addListener(map.value, 'idle', () => {
  const center = map.value.getCenter();

  if (!center || isNaN(center.lat()) || isNaN(center.lng())) {
    console.warn("Ungültiges Zentrum:", center);
    return;
  }

  const newCenter = {
    lat: center.lat(),
    lng: center.lng()
  };

  renderMarkers();
  emit('map-moved', newCenter);
});


  travelMode._value = google.maps.TravelMode.WALKING;

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
  travelMode._value = google.maps.TravelMode[mode];
}

let userMarker = null;
let watchId = null;

function startFollowingUser() {
  if (navigator.geolocation) {
    watchId = navigator.geolocation.watchPosition(
        position => {
          const pos = {
            lat: position.coords.latitude,
            lng: position.coords.longitude
          };

          if (!userMarker) {
            userMarker = new google.maps.Marker({
              map: map.value,
              position: pos,
              icon: {
                path: google.maps.SymbolPath.CIRCLE,
                scale: 8,
                fillColor: '#4285F4',
                fillOpacity: 1,
                strokeWeight: 2,
                strokeColor: '#ffffff',
              }
            });
          } else {
            userMarker.setPosition(pos);
          }

          if (
              map.value &&
              props.center &&
              typeof props.center.lat === 'number' &&
              typeof props.center.lng === 'number' &&
              isFinite(props.center.lat) &&
              isFinite(props.center.lng)
          ) {
            map.value.setCenter(props.center);
          }
          if (destination.value) {
            const dist = getDistanceMeters(pos, destination.value);
            if (dist < 20) {
              console.log("Cél elérve 🎉");
              stopFollowingUser();
            }
          }
        },
        error => {
          console.error(error);
        },
        {
          enableHighAccuracy: true,
          maximumAge: 0,
          timeout: 5000
        }
    );
  } else {
    console.error('geolocation denied from Browser');
  }
}

function stopFollowingUser() {
  if (watchId !== null) {
    navigator.geolocation.clearWatch(watchId);
    watchId = null;
  }
}

let destination = ref(null)
function drawRoute(from, to) {
  destination = to;
  directionsService = new google.maps.DirectionsService()
  directionsRenderer = new google.maps.DirectionsRenderer()
  directionsRenderer.setMap(map.value)

  directionsService.route(
      {
        origin: from,
        destination: to,
        travelMode: travelMode._value,
      },
      (result, status) => {
        if (status === "OK") {
          directionsRenderer.setDirections(result);
        }
      }
  );
  startFollowingUser();
}

function getDistanceMeters(pos1, pos2) {
  if (!pos1?.lat || !pos1?.lng || !pos2?.lat || !pos2?.lng) {
    return Infinity;
  }

  const R = 6371e3;
  const toRad = x => (x * Math.PI) / 180;

  const lat1 = toRad(pos1.lat);
  const lat2 = toRad(pos2.lat);
  const dLat = toRad(pos2.lat - pos1.lat);
  const dLng = toRad(pos2.lng - pos1.lng);

  const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.cos(lat1) * Math.cos(lat2) *
      Math.sin(dLng / 2) * Math.sin(dLng / 2);

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  const distance = R * c;

  return distance;
}



defineExpose({
  drawRoute,
  setTravelMode
});

watch(() => props.toilets, (newVal) => {
  renderMarkers();
});

</script>
