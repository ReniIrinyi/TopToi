<template>
  <div class="overlay">
    <div class="dialog">
      <div class="dialog-header">
        <h2>{{t("LABEL_ADD_TOILET")}}</h2>
        <button class="close-btn" @click="emit('close')">✖</button>
      </div>
      <form @submit.prevent="submitForm">
        <div style="margin-bottom: 6px;">
          <label for="name" style="display: block; font-weight: 500; margin-bottom: 4px;">{{ t('LABEL_NAME') }}</label>
          <input class="input-element" id="name" v-model="form.name" type="text" required style="width: 100%; padding: 6px;" />
        </div>

        <div style="margin-bottom: 6px;">
          <label for="price" style="display: block; font-weight: 500; margin-bottom: 4px;">{{ t('LABEL_PRICE') }}</label>
          <input class="input-element" id="price" v-model.number="form.priceCHF" type="number" style="width: 100%; padding: 6px;" />
        </div>
        <label><input type="checkbox" v-model="form.tags.BABY_ROOM" /> {{ t('LABEL_WICKELRAUM') }}</label>
        <label><input type="checkbox" v-model="form.tags.WHEELCHAIR_ACCESSIBLE" /> {{t('LABEL_ROLLSTUHLGERECHT')}}</label>

        <AddNote v-model:rating="rating" v-model:img="imageFile" v-model:note="noteText" ></AddNote>

        <button class="btn submit-btn" type="submit">{{ t('LABEL_SPEICHERN') }}</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import api from '../service/apiService.js';
import {translate} from "@/service/translationService.js";
import AddNote from "@/views/AddNote.vue";
import {ref} from "vue";
const form= ref( {
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
})

const rating = ref(0)
const imageFile = ref(null)
const noteText = ref('')

const t = translate;
const emit = defineEmits(['close']);

function submitForm() {
  console.log('submit..')
  navigator.geolocation.getCurrentPosition(
      (pos) => {
        form.latitude = pos.coords.latitude;
        form.longitude = pos.coords.longitude;

        const formData = new FormData();
        for (const key in this.form) {
          const val = this.form[key];
          if (typeof val === 'object') {
            formData.append(key, JSON.stringify(val));
          } else {
            formData.append(key, val);
          }
        }
        formData.append('note', noteText.value);
        formData.append('vote', rating.value);
        if (imageFile) {
          formData.append('image', imageFile?.value);
        }

        api.addToilet(formData).then(() => {
          console.log(formData)
          emit('close');
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
</script>

<style scoped>

label {
  display: block;
  margin: 8px 0;
}

.btn {
  background-color: var(--primary-color);
  color: var(--primary-background);
}

</style>
