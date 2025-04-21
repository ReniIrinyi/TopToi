<template>
  <div class="toilet-card">
      <button class="close-btn" @click="emit('close')">✖</button>
      <h2>{{  props.toilet.name }}</h2>

    <div class="distance" v-if=" props.toilet.distance">
      {{  props.toilet.distance }} m
    </div>

      <div class="tags-container">
        <div class="tags">
          <span v-if=" props.toilet.tags?.BABY_ROOM">🍼 {{t('LABEL_WICKELRAUM')}}</span>
          <span v-if=" props.toilet.tags?.WHEELCHAIR_ACCESSIBLE">♿{{t('LABEL_ROHLSTUHLGERECHT')}}</span>
          <span v-if=" props.toilet.priceCHF !== null">{{  props.toilet.priceCHF }} CHF</span>
          <span v-if=" props.toilet.openHours.length">{{  props.toilet.openHours }}</span>
          <div style="margin: 12px 0; text-align: center;">
            <span v-for="star in 5" :key="star" :style="{ fontSize: '24px', color: rating >= star ? '#ffd055' : '#bbb' }">★</span>
          </div>
        </div>
        <div class="add-note">
          <button @click="showNoteForm = !showNoteForm"> {{t('LABEL_ADD_NOTE')}}</button>
        </div>
      </div>

    <div class="tags">
      <button @click="setMode('WALKING')">🚶‍♂️ {{ t('LABEL_ZU_FUSS') }}</button>
      <button @click="setMode('DRIVING')">🚗 {{ t('LABEL_AUTO') }}</button>
      <button @click="setMode('BICYCLING')">🚴‍♀️ {{t('LABEL_VELO')}}</button>
    </div>
    <button class="nav-btn" @click="navigate">🧭 {{t('LABEL_ROUTE')}}</button>

    <section v-if=" props.toilet.notes?.length">
      <h3>{{ t('LABEL_NOTES') }}</h3>
      <div
          v-for="note in  props.toilet.notes"
          :key="note.addDate"
          class="note"
          :class="{ 'own-note': isOwnNote(note) }"
      >
        <p><strong v-if="isOwnNote(note)">{{ t('LABEL_OWN_NOTES') }}</strong> {{ note.text }}</p>
        <small>{{ note.addDate }}</small>
        <div v-if="note.imageUrl" class="note-image">
          <img :src="note.imageUrl" alt="Megjegyzés kép" />
        </div>
      </div>
    </section>

    <section>

      <div v-if="showNoteForm" class="note-form">
        <AddNote v-model:rating="rating" v-model:img="imageFile" v-model:note="noteText" ></AddNote>
        <button @click="submitNote">{{ t('LABEL_SAVE') }}</button>
      </div>
    </section>
  </div>
</template>

<script setup>
import {translate} from "@/service/translationService.js";
import {ref} from "vue";
import AddNote from "@/views/AddNote.vue";
import api from "@/service/apiService.js";

  const props = defineProps({
    currentUserId: Number,
    toilet: Object
  })

  const rating = ref(0)
  const imageFile = ref(null)
  const noteText = ref('')
  const emit = defineEmits(['navigate-to', 'travelMode',]);
  let showNoteForm = false

  const t = translate;

  function setMode(mode) {
    emit('travelMode', { mode });
  }

function navigate() {
  emit('navigate-to', {
    lat: props.toilet.latitude,
    lng: props.toilet.longitude
  });
}

function isOwnNote(note) {
  return note.userId === props.currentUserId;
}

function submitNote() {
  const noteData = {
    userId: props.currentUserId,
    toiletId:props.toilet,
    text: noteText.value,
    image: imageFile.value,
    rate:rating.value
  };
   api.addNote(noteData).then(() => {
     console.log(noteData)
     showNoteForm = false;
   }).catch(err => {
     console.error("API error:", err);
     alert(this.t('LABEL_SAVE_FAILED'));
   });
}


</script>

<style scoped>

.toilet-card {
  position: fixed;
  display: flex;
  flex-direction: column;
  bottom: 0;
  left: 0;
  right: 0;
  background: var(--primary-color);
  padding: 16px;
  color:var(--primary-background);
  border-radius: 16px 16px 0 0;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.2);
  z-index: 1500;
  max-height: 80vh;
  overflow-y: auto;
}

.tags-container {
  display: flex;
  color: var(--primary-color);
  font-weight: bold;
  font-size: 0.9em;
  justify-content: space-between;
  width: 100%;
  height: 34px;

  & .tags{
    display: flex;
    width: 100%;
  }

  & .tags span {
    margin-right: 10px;
    background: var(--primary-background);
    padding: 4px 8px;
    border-radius: 4px;
  }

  & .tags button{
    background: var(--primary-background);
    padding: 4px 8px;
    border-radius: 4px;
    border:none;
  }

 & .tags .ratings{
   display: flex;
   padding-inline: 2px;

   & button{
     border:none;
     background: var(--primary-background);
     padding: 4px 8px;
     color: var(--primary-color);
     border-radius: 4px;
     cursor: pointer;
   }
   & button:first-child{
     margin-right: 2px;
   }
 }

  & .add-note{
    display: flex;

    & button{
      display: flex;
      border:none;
      background: var(--primary-background);
      color: var(--primary-color);
      padding: 4px 8px;
      border-radius: 4px;
      cursor: pointer;
    }

  }

}

.nav-btn {
  width: 100%;
  margin: 10px 0;
  background: var(--primary-background);
  color: var(--primary-color);
  padding: 10px;
  border: none;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
}

.note {
  background:var(--primary-background);
  padding: 8px;
  border-radius: 6px;
  margin-bottom: 8px;
}

.note-image img {
  max-width: 100%;
  border-radius: 6px;
  margin-top: 6px;
}

.note-form {
  margin-top: 10px;
  background:var(--primary-background);
  padding: 10px;
  border-radius: 6px;
}

.note-form textarea {
  width: 100%;
  margin-bottom: 6px;
  padding: 6px;
  border-radius: 4px;
}

</style>
