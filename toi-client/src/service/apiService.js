import axios from 'axios';

const apiClient = axios.create({
    baseURL: 'http://localhost:8080',
});

// Automatically include JWT in headers if available
apiClient.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export default {
    // Public API
    getToilets(lat, lng) {
        return apiClient.get('/toilets', { params: { lat, lng } });
    },
    getToiletById(id) {
        return apiClient.get(`/toilets/${id}`);
    },

    // Authenticated API
    addToilet(data) {
        return apiClient.post('/toilets', data);
    },
    vote(toiletId, userId, vote) {
        return apiClient.post(`/toilets/${toiletId}/votes/${userId}`, { vote });
    },
    addNote(toiletId, userId, note) {
        return apiClient.post(`/toilets/${toiletId}/notes/${userId}`, { note });
    },
    updateNote(toiletId, userId, note) {
        return apiClient.put(`/toilets/${toiletId}/notes/${userId}`, { note });
    },
    deleteNote(toiletId, userId) {
        return apiClient.delete(`/toilets/${toiletId}/notes/${userId}`);
    },

    // Auth
    registerUser(data) {
        return apiClient.post('/user/register', data);
    },
    login(data) {
        return apiClient.post('/user/login', data);
    },
    loginWithGoogle(idToken) {
        return apiClient.post('/auth/google', { idToken });
    }
};