<template>
  <div class="overlay">
    <div class="dialog">
      <div class="dialog-header">
        <h2>Bejelentkezés</h2>
        <button class="close-btn" @click="$emit('close')">✖</button>
      </div>

      <form @submit.prevent="login">
        <input class="input-element" v-model="email" type="email" placeholder="Email" required />
        <input class="input-element" v-model="password" type="password" placeholder="Jelszó" required />
        <button class="btn submit-btn" type="submit">Bejelentkezés</button>
      </form>

      <GoogleOneTap :onLogin="handleGoogleLoginSuccess" />

    </div>
  </div>
</template>

<script>
import apiService from '@/service/apiService.js';
import GoogleOneTap from "@/views/GoogleOneTap.vue";

export default {
  components: {GoogleOneTap},
  data() {
    return {
      email: '',
      password: '',
    };
  },
  methods: {
    async login() {
      try {
        const response = await apiService.login({ email: this.email, password: this.password });
        const token = response.data.token;
        localStorage.setItem('token', token);
        localStorage.setItem('userEmail', this.email);
        this.$emit('logged-in', this.email);
      } catch (err) {
        alert('Hibás email vagy jelszó');
      }
    },
    handleGoogleLoginSuccess(token) {
      localStorage.setItem('token', token);
      this.$emit('logged-in', 'google_user');
    }
  },
};
</script>

<style scoped>

.btn {
  background-color: #3685d3;
  color: white;
}

button.google {
  background-color: #db4437;
}


</style>
