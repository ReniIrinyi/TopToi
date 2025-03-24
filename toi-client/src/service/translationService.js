import Papa from 'papaparse';

const translations = {};
let currentLang = 'hu';

export function setLanguage(lang) {
    currentLang = lang;
}

export function getLanguage() {
    return currentLang;
}

export async function loadTranslations() {
    const response = await fetch('../public/translations.csv');
        if (!response.ok) throw new Error("CSV not found");

        const csvText = await response.text();
        const result = Papa.parse(csvText, { header: true });

        result.data.forEach(row => {
            const key = row.key;
            if (!key) return;
            translations[key] = {
                hu: row.hu?.trim() || '',
                de: row.de?.trim() || '',
                en: row.en?.trim() || '',
            };
        });
    console.log('Translations loaded:', translations);

}

export function translate(key) {
    return translations[key]?.[currentLang] || key;
}
