<template>
  <header class="header">
    <div class="logo">
      <h1>TopToi</h1>
    </div>
    <div class="auth-section">
      <div class="profile-dropdown" @click="toggleDropdown">
        <img class="avatar" :src="userAvatar" alt="avatar" />
        <div class="dropdown" v-if="dropdownOpen">
          <template v-if="!isLoggedIn">
            <button @click="showLogin = true">Bejelentkezés</button>
            <button @click="showRegister = true" class="secondary">Regisztráció</button>
          </template>
          <template v-else>
            <p><strong>{{ userName }}</strong></p>
            <p>{{ userEmail }}</p>
            <hr />
            <button @click="logout">Kijelentkezés</button>
          </template>
        </div>
      </div>
    </div>

    <LoginDialog v-if="showLogin" @close="showLogin = false" @logged-in="onLogin" />
    <RegisterDialog v-if="showRegister" @close="showRegister = false" @registered="onRegister" />
  </header>
</template>

<script>
import LoginDialog from './LoginForm.vue';
import RegisterDialog from './RegisterForm.vue';

export default {
  components: { LoginDialog, RegisterDialog },
  data() {
    return {
      showLogin: false,
      showRegister: false,
      dropdownOpen: false,
      userEmail: localStorage.getItem('userEmail') || null,
      userName: localStorage.getItem('userName') || 'Felhasználó',
      userAvatar: localStorage.getItem('userAvatar') || 'https://www.gravatar.com/avatar?d=mp',
    };
  },
  computed: {
    isLoggedIn() {
      return !!localStorage.getItem('token');
    },
  },
  methods: {
    onLogin(email) {
      this.userEmail = email;
      this.userName = localStorage.getItem('userName') || 'Felhasználó';
      this.userAvatar = localStorage.getItem('userAvatar') || 'https://www.gravatar.com/avatar?d=mp';
      this.showLogin = false;
    },
    onRegister(email) {
      this.userEmail = email;
      this.showRegister = false;
    },
    toggleDropdown() {
      this.dropdownOpen = !this.dropdownOpen;
    },
    logout() {
      localStorage.clear();
      this.userEmail = null;
      this.userName = '';
      this.userAvatar = '';
      this.dropdownOpen = false;
      location.reload();
    },
  },
};
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #008080;
  color: white;
  padding: 10px 20px;
}

.logo h1 {
  margin: 0;
}

.wc-toggle {
  background: white;
  color: #008080;
  font-weight: bold;
  border: none;
  padding: 8px 12px;
  border-radius: 30px;
  cursor: pointer;
  font-size: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.auth-section {
  position: relative;
}

.profile-dropdown {
  position: relative;
  cursor: pointer;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid white;
}

.dropdown {
  position: absolute;
  right: 0;
  background: white;
  color: black;
  margin-top: 10px;
  padding: 10px;
  border-radius: 6px;
  width: 200px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  z-index: 100;
}
.dropdown p {
  margin: 5px 0;
}
.dropdown button {
  background: #008080;
  color: white;
  border: none;
  padding: 6px;
  width: 100%;
  border-radius: 4px;
  margin-top: 6px;
  cursor: pointer;
}
.dropdown .secondary {
  background: #f0f0f0;
  color: #555;
}
</style>
