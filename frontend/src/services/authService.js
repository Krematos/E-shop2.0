import api from './api';

/**
 * Získání CSRF tokenu z nového endpointu /api/csrf/token.
 * Backend vrátí: { token, headerName, parameterName }
 * Token se uloží do axios defaults jako záloha (axios ho čte i z cookie automaticky).
 */
const ensureCsrfToken = async () => {
  const response = await api.get('/csrf/token');
  if (response.data?.token) {
    api.defaults.headers.common['X-XSRF-TOKEN'] = response.data.token;
  }
};

/**
 * Registrace nového uživatele
 */
export const register = async (userData) => {
  await ensureCsrfToken(); // Získání CSRF tokenu před registrací
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
  await ensureCsrfToken(); // Získání CSRF tokenu před přihlášením

  const response = await api.post('/auth/login', {
    username,
    password,
  });

  // Token je nyní v HttpOnly cookie, ukládá pouze user data
  if (response.data.username) {
    localStorage.setItem('user', JSON.stringify({
      username: response.data.username,
      roles: response.data.roles,
    }));
  }

  return response.data;
};

/**
 * Odhlášení uživatele
 */
export const logout = () => {
  localStorage.removeItem('user');
};

/**
 * Ověření tokenu
 */
export const validateToken = async () => {
  try {
    const response = await api.get('/auth/validate');
    return response.data;
  } catch (error) {
    console.error('Error validating token:', error);
    throw error;
  }
};

/**
    * Získání role uživatele
    */
export const getUserRole = async () => {
try {
    const response = await api.get('/auth/role');
    return response.data.role;
  } catch (error) {
    console.error('Error fetching user role:', error);
    throw error;
  }
};

export const requestPasswordReset = async (email) => {
  await ensureCsrfToken(); // Získání CSRF tokenu před odesláním požadavku
  const response = await api.post('/auth/forgot-password', { email });
  return response.data;
};

export default {
  register,
  login,
  logout,
  validateToken,
  getUserRole,
};

