<template>
  <div class="toilet-card">
    <h2>{{ toilet.name }}</h2>
    <div class="tags">
      <span v-if="toilet.tags?.BABY_ROOM">🍼 Pelenkázó</span>
      <span v-if="toilet.tags?.WHEELCHAIR_ACCESSIBLE">♿ Akadálymentes</span>
      <span v-if="toilet.priceHUF">{{ toilet.priceHUF }} HUF</span>
      <span v-if="toilet.openHours">{{ toilet.openHours }}</span>
    </div>

    <div class="mode-buttons">
      <button @click="setMode('WALKING')">🚶‍♂️ Gyalog</button>
      <button @click="setMode('DRIVING')">🚗 Autó</button>
      <button @click="setMode('BICYCLING')">🚴‍♀️ Bicikli</button>
    </div>


    <button class="nav-btn" @click="navigate">📍 Útvonal</button>

    <section>
      <h3>Értékelések</h3>
      <p>👍 {{ positiveVotes }} | 👎 {{ negativeVotes }}</p>
    </section>

    <section v-if="toilet.notes?.length">
      <h3>Megjegyzések</h3>
      <div v-for="note in toilet.notes" :key="note.addDate" class="note">
        <p>{{ note.text }}</p>
        <small>{{ note.addDate }}</small>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  props: ['toilet'],
  computed: {
    positiveVotes() {
      return this.toilet.votes?.filter(v => v.value > 0).length || 0;
    },
    negativeVotes() {
      return this.toilet.votes?.filter(v => v.value < 0).length || 0;
    },
  },
  methods: {
  setMode(mode) {
    this.$emit('travelMode', {mode})
  },
  navigate() {
      this.$emit('navigate-to', {
        lat: this.toilet.latitude,
        lng: this.toilet.longitude
      });
    },
  },
};
</script>

<style scoped>
.toilet-card {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #008080;
  padding: 16px;
  color:white;
  border-radius: 16px 16px 0 0;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.2);
  z-index: 1500;
  max-height: 80vh;
  overflow-y: auto;
}

.tags span {
  margin-right: 10px;
  display: inline-block;
  background: #e0f7f7;
  padding: 4px 8px;
  border-radius: 4px;
  color: #008080;
  font-weight: bold;
  font-size: 0.9em;
}

.nav-btn {
  width: 100%;
  margin: 10px 0;
  background: #e0f7f7;
  color: #008080;
  padding: 10px;
  border: none;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
}

.note {
  background: #f7f7f7;
  padding: 8px;
  border-radius: 6px;
  margin-bottom: 8px;
}
</style>
