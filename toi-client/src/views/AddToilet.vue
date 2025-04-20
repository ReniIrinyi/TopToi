<template>
  <div class="overlay">
    <div class="dialog">
      <div class="dialog-header">
        <h2>{{this.t("LABEL_ADD_TOILET")}}</h2>
        <button class="close-btn" @click="$emit('close')">✖</button>
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
        <label><input type="checkbox" v-model="form.tags.WHEELCHAIR_ACCESSIBLE" /> {{this.t('LABEL_ROLLSTUHLGERECHT')}}</label>

        <div>
          <textarea class="input-element" v-model="noteText" placeholder="Schildere anderen deine Meinung zu dieser Toalet ..." rows="3" style="width:100%; margin-top:8px;"></textarea>
        </div>

        <div style="margin: 12px 0; text-align: center;">
        <span v-for="star in 5" :key="star" @click="setRating(star)" :style="{ cursor: 'pointer', fontSize: '24px', color: rating >= star ? '#ffd055' : '#bbb' }">★</span>
        </div>

        <button class="btn submit-btn" type="button" @click="triggerFileInput">📸 Fotos hinzufügen</button>
        <input style="display:none" type="file" accept="image/*" capture="environment" @change="handleFile" />

        <button class="btn submit-btn" type="submit">{{ this.t('LABEL_SPEICHERN') }}</button>
      </form>
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
      },
      noteText: '',
      rating: 0,
      imageFile: null
    };
  },
  computed: {
    t(){
      return translate;
    },
  },
  methods: {
    triggerFileInput() {
      this.$refs.fileInput.click();
    },
    handleFile(event) {
      this.imageFile = event.target.files[0];
    },
    setRating(star) {
      this.rating = star;
      this.form.rating = star;
    },
    submitForm() {
      console.log('submit..')
      navigator.geolocation.getCurrentPosition(
          (pos) => {
            this.form.latitude = pos.coords.latitude;
            this.form.longitude = pos.coords.longitude;


            const formData = new FormData();
            for (const key in this.form) {
              const val = this.form[key];
              if (typeof val === 'object') {
                formData.append(key, JSON.stringify(val));
              } else {
                formData.append(key, val);
              }
            }
            formData.append('note', this.noteText);
            formData.append('vote', this.vote);
            if (this.imageFile) {
              formData.append('image', this.imageFile);
            }

            api.addToilet(formData).then(() => {
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

label {
  display: block;
  margin: 8px 0;
}

.btn {
  background-color: var(--primary-color);
  color: var(--primary-background);
}

</style>
