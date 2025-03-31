<template>
  <div class="overlay">
    <div class="dialog">
      <h2>Bejelentkezés</h2>
      <form @submit.prevent="login">
        <input v-model="email" type="email" placeholder="Email" required />
        <input v-model="password" type="password" placeholder="Jelszó" required />
        <button type="submit">Bejelentkezés</button>
      </form>

      <div class="divider">vagy</div>

      <GoogleOneTap :onLogin="handleGoogleLoginSuccess" />

      <button class="close" @click="$emit('close')">Bezárás</button>
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
input {
  display: block;
  width: 100%;
  margin: 10px 0;
  padding: 8px;
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
button.google {
  background-color: #db4437;
}
button.close {
  background-color: #aaa;
}
.divider {
  text-align: center;
  margin: 10px 0;
  color: #666;
}
</style>
