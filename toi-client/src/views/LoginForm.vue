<template>
  <div class="overlay">
    <div class="dialog">
      <div class="dialog-header">
        <h2>{{t('LABEL_LOGIN')}}</h2>
        <button class="close-btn" @click="emit('close')">✖</button>
      </div>

      <form @submit.prevent="login">
        <input class="input-element" v-model="email" type="email" :placeholder="t('LABEL_EMAIL')" required />
        <input class="input-element" v-model="password" type="password" :placeholder="t('LABEL_PASSWORD')" required />
        <button class="btn submit-btn" type="submit">{{ t('LABEL_CONFIRM') }}</button>
      </form>

      <div class="divider">{{ t('LABEL_OR_CONTINUE_WITH') }}</div>

      <GoogleOneTap :onLogin="handleGoogleLoginSuccess" />

    </div>
  </div>
</template>

<script setup>
import apiService from '@/service/apiService.js';
import GoogleOneTap from "@/views/GoogleOneTap.vue";
import {translate} from "@/service/translationService.js";
import {ref} from "vue";

const email = ref('')
const password = ref('')

const t = translate

const emit = defineEmits(['logged-in', 'google_user', 'close'])

 async function login() {
  try {
    const response = await apiService.login({ email: email.value, password: password.value });
    const token = response.data.token;
    localStorage.setItem('token', token);
    localStorage.setItem('userEmail', email.value);
    emit('logged-in', email.value);
  } catch (err) {
    alert(t('ERROR_AUTHENTIFICATION_ERROR'));
  }
}

function handleGoogleLoginSuccess(token) {
  localStorage.setItem('token', token);
  emit('logged-in', 'google_user');
}

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
