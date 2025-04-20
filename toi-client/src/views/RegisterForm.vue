<template>
  <div class="overlay">
    <div class="dialog">
      <div class="dialog-header">
        <h2>Regisztráció</h2>
        <button class="close-btn" @click="$emit('close')">✖</button>
      </div>

      <form @submit.prevent="register">
        <input class="input-element" v-model="email" type="email" placeholder="Email" required />
        <input class="input-element" v-model="password" type="password" placeholder="Jelszó" required />
        <input class="input-element" v-model="confirmPassword" type="password" placeholder="Jelszó megerősítése" required />
        <button class="btn submit-btn" type="submit">Regisztráció</button>
      </form>

      <GoogleOneTap :onLogin="handleGoogleLoginSuccess" />

    </div>
  </div>
</template>

<script>
import apiService from '@/service/apiService.js';
import GoogleOneTap from './GoogleOneTap.vue';

export default {
  name: "RegisterDialog",
  components: {
    GoogleOneTap
  },
  data() {
    return {
      email: '',
      password: '',
      confirmPassword: ''
    };
  },
  methods: {
    async register() {
      if (this.password !== this.confirmPassword) {
        alert("A jelszavak nem egyeznek!");
        return;
      }

      try {
        const res = await apiService.registerUser({
          email: this.email,
          password: this.password
        });

        if (res.status === 201) {
          alert("Sikeres regisztráció!");
          this.$emit('logged-in', this.email);
        }
      } catch (err) {
        alert("Ez az e-mail már létezik vagy hiba történt!");
      }
    },
    handleGoogleLoginSuccess(token) {
      localStorage.setItem('token', token);
      this.$emit('logged-in', 'google_user');
    }
  }
};
</script>

<style scoped>

.btn {
  background-color: var(--primary-color);
  color: var(--primary-background);
}

button.google {
  background-color: #db4437;
}
</style>
