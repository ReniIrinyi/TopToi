<template>
  <div class="overlay">
    <div class="dialog">
      <h2>{{this.t("LABEL_ADD_TOILET")}}</h2>
      <form @submit.prevent="submitForm">
        <div style="margin-bottom: 6px;">
          <label for="name" style="display: block; font-weight: 500; margin-bottom: 4px;">{{ t('LABEL_NAME') }}</label>
          <input id="name" v-model="form.name" type="text" required style="width: 100%; padding: 6px;" />
        </div>

        <div style="margin-bottom: 6px;">
          <label for="price" style="display: block; font-weight: 500; margin-bottom: 4px;">{{ t('LABEL_PRICE') }}</label>
          <input id="price" v-model.number="form.priceCHF" type="number" style="width: 100%; padding: 6px;" />
        </div>
        <label><input type="checkbox" v-model="form.tags.BABY_ROOM" /> {{ t('LABEL_WICKELRAUM') }}</label>
        <label><input type="checkbox" v-model="form.tags.WHEELCHAIR_ACCESSIBLE" /> {{this.t('LABEL_ROLLSTUHLGERECHT')}}</label>
        <button type="submit">{{ this.t('LABEL_SPEICHERN') }}</button>
      </form>
      <button class="close" @click="$emit('close')">{{ this.t('LABEL_CLOSE') }}</button>
    </div>
  </div>
</template>

<script>
import api from '../service/apiService.js';
import {translate} from "@/service/translationService.js";

export default {
  data() {
    return {
      form: {
        name: '',
        addDate: new Date().toISOString(),
        category: 'public',
        openHours: [],
        tags: { BABY_ROOM: false, WHEELCHAIR_ACCESSIBLE: false },
        entryMethod: 'free',
        priceCHF: 0,
        code: '',
        latitude: null,
        longitude: null,
      }
    };
  },
  computed: {
    t(){
      return translate;
    },
  },
  methods: {
    submitForm() {
      console.log('submit..')
      navigator.geolocation.getCurrentPosition(
          (pos) => {
            this.form.latitude = pos.coords.latitude;
            this.form.longitude = pos.coords.longitude;

            api.addToilet(this.form).then(() => {
              this.$emit('close');
            }).catch(err => {
              console.error("API error:", err);
              alert(this.t('LABEL_SAVE_FAILED'));
            });
          },
          (err) => {
            console.error("Geolocation error:", err);
            alert(this.t('LABEL_GEOLOCATION_DENIED'));
          }
      );
    }

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
  color: #181818;
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
