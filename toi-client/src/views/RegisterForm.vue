<template>
  <div class="overlay">
    <div class="dialog">
      <div class="dialog-header">
        <h2>{{ t('LABEL_REGISTRATION') }}</h2>
        <button class="close-btn" @click="emit('close')">✖</button>
      </div>

      <form @submit.prevent="register">
        <input class="input-element" v-model="email" type="email" :placeholder="t('LABEL_EMAIL')" required />
        <input class="input-element" v-model="password" type="password" :placeholder="t('LABEL_PASSWORD')" required />
        <button class="btn submit-btn" type="submit">📧 {{ t('LABEL_CONTINUE_WITH_EMAIL') }}</button>
      </form>

      <div class="divider">{{ t('LABEL_OR_CONTINUE_WITH') }}</div>

      <GoogleOneTap :onLogin="handleGoogleLoginSuccess" />    </div>
  </div>
</template>

<script setup>
import apiService from '@/service/apiService.js';
import GoogleOneTap from './GoogleOneTap.vue';
import {ref} from 'vue';
import {translate} from '@/service/translationService.js';

const t = translate;
const emit = defineEmits(['logged-in', 'close']);

const email = ref('');
const password = ref('');

async function register() {
  try {
    const res = await apiService.registerUser({
      email: email.value,
      password: password.value
    });

    if (res.status === 200) {
      alert(t('WARN_AUTHENTIFICATION_SUCCESS'));
      localStorage.setItem('userEmail', email.value);
      emit('logged-in', email.value);
    }
  } catch (err) {
    alert(t('ERROR_DUPLICATE_EMAIL'));
  }
}

function handleGoogleLoginSuccess(token) {
  localStorage.setItem('token', token);
  emit('logged-in', 'close');
}

</script>

<style scoped>
.dialog {
  max-width: 400px;
}

.icon {
  width: 20px;
  margin-right: 8px;
  vertical-align: middle;
}

.btn {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--primary-color);
  color: white;
  padding: 10px;
  border: none;
  border-radius: 6px;
  margin-top: 10px;
  cursor: pointer;
}

.btn.google {
  background-color: white;
  color: #444;
  border: 1px solid #ccc;
}

.divider {
  text-align: center;
  margin: 15px 0;
  font-size: 14px;
  color: #888;
}
</style>
