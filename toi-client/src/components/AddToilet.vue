<template>
  <div class="overlay">
    <div class="dialog">
      <h2>Mosdó hozzáadása</h2>
      <form @submit.prevent="submitForm">
        <input v-model="form.name" required placeholder="Mosdó neve" />
        <input v-model.number="form.price" type="number" placeholder="Ár (CHF)" />
        <label><input type="checkbox" v-model="form.tags.BABY_ROOM" /> Pelenkázó</label>
        <label><input type="checkbox" v-model="form.tags.WHEELCHAIR_ACCESSIBLE" /> Akadálymentes</label>
        <button type="submit">Hozzáadás</button>
      </form>
      <button class="close" @click="$emit('close')">Bezárás</button>
    </div>
  </div>
</template>

<script>
import api from '../service/apiService.js';

export default {
  data() {
    return {
      form: {
        name: '',
        price: 0,
        tags: {BABY_ROOM: false, WHEELCHAIR_ACCESSIBLE: false},
        latitude: null,
        longitude: null,
      },
    };
  },
  methods: {
    submitForm() {
      navigator.geolocation.getCurrentPosition((pos) => {
        this.form.latitude = pos.coords.latitude;
        this.form.longitude = pos.coords.longitude;

        api.addToilet(this.form).then(() => {
          alert('Mosdó sikeresen hozzáadva!');
          this.$emit('close'); // zárja be automatikusan a popupot
        });
      });
    },
  },
};
</script>

<style scoped>
.overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog {
  background: white;
  padding: 20px;
  border-radius: 8px;
  min-width: 300px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);
}

input[type="text"],
input[type="number"] {
  display: block;
  width: 100%;
  margin: 10px 0;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

label {
  display: block;
  margin: 8px 0;
}

button {
  width: 100%;
  padding: 8px;
  margin-top: 10px;
  background-color: #008080;
  color: white;
  border: none;
  border-radius: 4px;
  font-weight: bold;
  cursor: pointer;
}

button.close {
  background-color: #aaa;
}
</style>
