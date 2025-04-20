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
            <button @click="showLogin = true">{{this.t('LABEL_LOGIN')}}</button>
            <button @click="showRegister = true" class="secondary">{{ this.t('LABEL_REGISTRATION') }}</button>
          </template>
          <template v-else>
            <p><strong>{{ userName }}</strong></p>
            <p>{{ userEmail }}</p>
            <hr />
            <button @click="logout">{{this.t('LABEL_LOGOUT')}}</button>
          </template>
        </div>
      </div>
    </div>
    <div class="language-selector" @click="toggleLanguageDropdown">
      <span class="flag-icon">{{ getFlagIcon(currentLanguage) }}</span>
      <div class="language-dropdown" v-if="languageDropdownOpen">
        <div
            v-for="lang in getAvailableLanguages()"
            :key="lang"
            class="language-option"
            @click.stop="changeLanguage(lang)"
        >
          {{ getFlagIcon(lang) }}
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
import {translate, getLanguage, setLanguage} from "@/service/translationService.js";
export default {
  components: { LoginDialog, RegisterDialog },
  data() {
    return {
      showLogin: false,
      showRegister: false,
      dropdownOpen: false,
      languageDropdownOpen: false,
      currentLanguage: getLanguage(),
      userEmail: localStorage.getItem('userEmail') || null,
      userName: localStorage.getItem('userName') || 'Felhasználó',
      userAvatar: localStorage.getItem('userAvatar') || 'https://www.gravatar.com/avatar?d=mp',
    };
  },
  mounted() {
  console.log('component Header rendered... ')
  },
  computed: {
    t(){
      return translate;
    },
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
    getFlagIcon(lang) {
      switch (lang) {
        case 'hu': return '🇭🇺';
        case 'en': return '🇬🇧';
        case 'de': return '🇩🇪';
        default: return '🌐';
      }
    },
    getAvailableLanguages() {
      return ['hu', 'en', 'de'].filter(lang => lang !== this.currentLanguage);
    },
    toggleLanguageDropdown() {
      this.languageDropdownOpen = !this.languageDropdownOpen;
    },
    changeLanguage(lang) {
      this.currentLanguage = lang;
      setLanguage(lang);
      this.languageDropdownOpen = false;
    }
  },
};
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: var(--primary-color);
  color: var(--primary-background);
  padding: 0 20px;
  height: 100%;
}

.logo h1 {
  margin: 0;
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
  background: var(--primary-background);
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
  background: var(--primary-color);
  color: var(--primary-background);
  border: none;
  padding: 6px;
  width: 100%;
  border-radius: 4px;
  margin-top: 6px;
  cursor: pointer;
}
.dropdown .secondary {
  background: var(--primary-background);
  color: #555;

  .language-selector {
    position: relative;
    margin-left: 20px;
    cursor: pointer;
    font-size: 20px;
    user-select: none;
  }

  .flag-icon {
    font-size: 22px;
  }

  .language-dropdown {
    position: absolute;
    right: 0;
    margin-top: 10px;
    background: var(--primary-background);
    color: black;
    padding: 8px;
    border-radius: 6px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
    z-index: 100;
  }

  .language-option {
    font-size: 22px;
    padding: 6px 10px;
    cursor: pointer;
    white-space: nowrap;
  }

  .language-option:hover {
    background-color: var(--primary-background);
    border-radius: 4px;
  }



}
</style>
