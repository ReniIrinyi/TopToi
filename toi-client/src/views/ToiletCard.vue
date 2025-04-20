<template>
  <div class="toilet-card">
      <button class="close-btn" @click="$emit('close')">✖</button>
      <h2>{{ toilet.name }}</h2>

    <div class="distance" v-if="toilet.distance">
      {{ toilet.distance }} m
    </div>

      <div class="tags-container">
        <div class="tags">
          <span v-if="toilet.tags?.BABY_ROOM">🍼 {{this.t('LABEL_WICKELRAUM')}}</span>
          <span v-if="toilet.tags?.WHEELCHAIR_ACCESSIBLE">♿{{this.t('LABEL_ROHLSTUHLGERECHT')}}</span>
          <span v-if="toilet.priceCHF !== null">{{ toilet.priceCHF }} CHF</span>
          <span v-if="toilet.openHours.length">{{ toilet.openHours }}</span>
          <div class="ratings">
            <button @click="submitVote(1)" :disabled="hasVoted">👍 {{ positiveVotes }}</button>
            <button @click="submitVote(-1)" :disabled="hasVoted">👎 {{ negativeVotes }}</button>
          </div>
        </div>
        <div class="add-note">
          <button @click="showNoteForm = !showNoteForm"> {{this.t('LABEL_ADD_NOTE')}}</button>
        </div>
      </div>

    <div class="tags">
      <button @click="setMode('WALKING')">🚶‍♂️ {{ this.t('LABEL_ZU_FUSS') }}</button>
      <button @click="setMode('DRIVING')">🚗 {{ this.t('LABEL_AUTO') }}</button>
      <button @click="setMode('BICYCLING')">🚴‍♀️ {{this.t('LABEL_VELO')}}</button>
    </div>
    <button class="nav-btn" @click="navigate">🧭 {{this.t('LABEL_ROUTE')}}</button>

    <section v-if="toilet.notes?.length">
      <h3>Megjegyzések</h3>
      <div
          v-for="note in toilet.notes"
          :key="note.addDate"
          class="note"
          :class="{ 'own-note': isOwnNote(note) }"
      >
        <p><strong v-if="isOwnNote(note)">Saját jegyzet:</strong> {{ note.text }}</p>
        <small>{{ note.addDate }}</small>
        <div v-if="note.imageUrl" class="note-image">
          <img :src="note.imageUrl" alt="Megjegyzés kép" />
        </div>
      </div>
    </section>

    <section>

      <div v-if="showNoteForm" class="note-form">
        <textarea v-model="newNoteText" placeholder="Írd be a megjegyzésed..." rows="3"></textarea>
        <input type="file" @change="handleImageUpload" />
        <button @click="submitNote">Mentés</button>
      </div>
    </section>
  </div>
</template>

<script>
import {translate} from "@/service/translationService.js";

export default {
  props: ['toilet', 'currentUserId'],
  data() {
    return {
      newNoteText: '',
      newNoteImage: null,
      showNoteForm: false,
    };
  },
  computed: {
    t() {
      return translate;
    },
    positiveVotes() {
      return this.toilet.votes?.filter(v => v.value > 0).length || 0;
    },
    negativeVotes() {
      return this.toilet.votes?.filter(v => v.value < 0).length || 0;
    },
    hasVoted() {
      return this.toilet.votes?.some(v => v.userId === this.currentUserId);
    }
  },
  methods: {
    setMode(mode) {
      this.$emit('travelMode', { mode });
    },
    navigate() {
      this.$emit('navigate-to', {
        lat: this.toilet.latitude,
        lng: this.toilet.longitude
      });
    },
    isOwnNote(note) {
      return note.userId === this.currentUserId;
    },
    handleImageUpload(e) {
      this.newNoteImage = e.target.files[0];
    },
    submitNote() {
      const noteData = {
        text: this.newNoteText,
        image: this.newNoteImage
      };
      this.$emit('submit-note', noteData);
      this.newNoteText = '';
      this.newNoteImage = null;
      this.showNoteForm = false;
    },
    submitVote(value) {
      this.$emit('submit-vote', value);
    }
  },
};
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
