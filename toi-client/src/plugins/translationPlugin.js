import {translate} from "@/service/translationService.js";

export default {
    install(app) {
        app.config.globalProperties.$translate = translate;
    }
};
