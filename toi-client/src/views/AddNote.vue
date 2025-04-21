<template>
  <div>
    <textarea class="input-element" v-model="noteText" placeholder="Schildere anderen deine Meinung zu dieser Toalet ..." rows="3" style="width:100%; margin-top:8px;"></textarea>
  </div>

  <div style="margin: 12px 0; text-align: center;">
    <span v-for="star in 5" :key="star" @click="setRating(star)" :style="{ cursor: 'pointer', fontSize: '24px', color: rating >= star ? '#ffd055' : '#bbb' }">★</span>
  </div>

  <button class="btn submit-btn" type="button" @click="triggerFileInput">📸 Fotos hinzufügen</button>
  <input style="display:none" type="file" accept="image/*" capture="environment" @change="handleFile" />
</template>

<script setup>

import {translate} from "@/service/translationService.js";
import {ref, watch} from "vue";

const noteText = ref('');
const rating = ref(0);
const imageFile = ref(null);

const emit = defineEmits(['update:rating', 'update:img', 'update:note'])

  const t =  translate

  function setRating(star) {
    rating.value = star;
    emit('update:rating',star)
  }

  function triggerFileInput() {
    this.$refs.fileInput.click();
  }

  function handleFile(event) {
    imageFile.value = event.target.files[0];
    emit('update:img'. event.target.files[0])
  }

  watch(noteText, (val) => {
    emit('update:note', val)
  })

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