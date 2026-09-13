import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
    'Cache-Control': 'no-cache, no-store, must-revalidate',
    'Pragma': 'no-cache',
    'Expires': '0',
  },
  withCredentials: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
});

// ── CSRF interceptor ────────────────────────────────────────────────────────
// Automaticky zajistí CSRF token před každou mutující operací.
// Token se načte jednou a cachuje; po 403 se automaticky obnoví a request se zopakuje.

const MUTATION_METHODS = new Set(['post', 'put', 'delete', 'patch']);

const fetchAndSetCsrfToken = async () => {
  // Voláme přímo fetch, aby request nespustil tento interceptor znovu (GET je mimo)
  const response = await api.get('/csrf/token');
  if (response.data?.token) {
    api.defaults.headers.common['X-XSRF-TOKEN'] = response.data.token;
    return response.data.token;
  }
  return null;
};

// Request interceptor – načte token se, pokud ještě není k dispozici
api.interceptors.request.use(async (config) => {
  const method = config.method?.toLowerCase();
  const isMutation = MUTATION_METHODS.has(method);
  const isCsrfEndpoint = config.url?.includes('/csrf/token');

  // Zajistíme token z default hlaviček nebo jej rovnou vyzvedneme
  let currentToken = api.defaults.headers.common['X-XSRF-TOKEN'];

  if (isMutation && !isCsrfEndpoint && !currentToken) {
    currentToken = await fetchAndSetCsrfToken();
  }

  // Explicitně přidáme hlavičku do aktuálního configu (axios sloučil defaulty při vzniku requestu,
  // takže pokud v defaults token předtím nebyl, config ho ještě nemá)
  if (isMutation && !isCsrfEndpoint && currentToken) {
    config.headers['X-XSRF-TOKEN'] = currentToken;
  }

  return config;
});

// Response interceptor – 401 redirect + 403 CSRF token refresh + retry
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const status = error.response?.status;

    // Session vypršela nebo přístup odepřen bez přihlášení
    if (status === 401) {
      // Ignorovat automatický redirect, pokud jde o samotný pokus o přihlášení
      const isLoginRequest = error.config.url && error.config.url.includes('/auth/login');
      
      if (!isLoginRequest) {
        localStorage.clear();
        sessionStorage.clear();
        window.location.replace('/login');
      }
      return Promise.reject(error);
    }

    // CSRF token vypršel nebo byl neplatný – obnov a zopakuj request (max 1 retry)
    if (status === 403 && !error.config._csrfRetried) {
      error.config._csrfRetried = true;
      delete api.defaults.headers.common['X-XSRF-TOKEN']; // vymaž starý token
      const newToken = await fetchAndSetCsrfToken();
      if (newToken) {
        error.config.headers['X-XSRF-TOKEN'] = newToken; // aktualizuj token v chybném requestu!
      }
      return api.request(error.config);
    }

    return Promise.reject(error);
  }
);

export default api;
