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
  await api.get('/auth/csrf'); // Získání CSRF tokenu před přihlášením

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

