import api from './api';

/**
 * Registrace nového uživatele
 */
export const register = async (userData) => {
  const response = await api.post('/auth/register', {
    username: userData.username,
    email: userData.email,
    password: userData.password,
  });
  return response.data;
};

/**
 * Přihlášení uživatele
 */
export const login = async (username, password) => {
  const response = await api.post('/auth/login', { username, password });

  // JWT je v HttpOnly cookie, ukládáme jen zobrazitelná user data
  if (response.data.username) {
    localStorage.setItem(
      'user',
      JSON.stringify({
        username: response.data.username,
        roles: response.data.roles,
      })
    );
  }

  return response.data;
};

/**
 * Odhlášení uživatele – čistí lokální stav.
 * Backend session (HttpOnly cookie) se invaliduje přes /auth/logout.
 */
export const logout = async () => {
  try {
    await api.post('/auth/logout');
  } catch (error) {
    console.error('Došlo k chybě při odhlašování na serveru:', error);
  } finally {
    localStorage.clear();
    sessionStorage.clear();
  }
};

/**
 * Ověření platnosti tokenu (JWT cookie)
 */
export const validateToken = async () => {
  const response = await api.get('/auth/validate');
  return response.data;
};

/**
 * Získání role přihlášeného uživatele
 */
export const getUserRole = async () => {
  const response = await api.get('/auth/role');
  return response.data.role;
};

/**
 * Odeslání žádosti o reset hesla
 */
export const requestPasswordReset = async (email) => {
  const response = await api.post('/auth/forgot-password', { email });
  return response.data;
};
