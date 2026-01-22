import axios from 'axios';

// Vytvoření Axios instance s base URL z environment proměnné
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Důležité pro posílání cookies
});

// Interceptor pro zpracování chyb
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token je neplatný nebo vypršel - cookie bude smazána backendem
      localStorage.removeItem('user');
      globalThis.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;

