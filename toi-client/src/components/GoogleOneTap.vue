<template>
  <div>
    <div
        id="g_id_onload"
        :data-client_id="clientId"
        data-auto_prompt="true"
        data-callback="handleGoogleLogin"
    ></div>
    <div class="g_id_signin" data-type="standard"></div>
  </div>
</template>

<script>
import apiService from "@/service/apiService.js";

export default {
  name: "GoogleOneTap",
  props: ['onLogin'],
  data() {
    return {
      clientId: '1030506683349-ism7bd2gihcggefm9gmdsejb6lelcq6d.apps.googleusercontent.com',
    };
  },
  mounted() {
    window.handleGoogleLogin = (response) => {
      const idToken = response.credential;

      apiService.loginWithGoogle(idToken)
          .then(res => {
            const token = res.data.token;
            localStorage.setItem('token', token);

            if (this.onLogin) {
              this.onLogin(token);
            }
          })
          .catch(() => {
            alert("Sikertelen bejelentkezés Google ID tokennel");
          });
    };

    if (!window.google) {
      const script = document.createElement("script");
      script.src = "https://accounts.google.com/gsi/client";
      script.async = true;
      script.defer = true;
      document.head.appendChild(script);
    }
  }
};
</script>
