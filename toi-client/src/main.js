import './assets/main.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from "@/router/index.js";
import {loadTranslations, setLanguage} from "@/service/translationService.js";
import translationPlugin from "@/plugins/translationPlugin.js";

await loadTranslations();
setLanguage('de');

const app = createApp(App);

app.use(router);
app.use(translationPlugin);

app.mount('#app');