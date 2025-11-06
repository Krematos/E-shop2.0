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
  const response = await api.post('/auth/login', {
    username,
    password,
  });
  
  if (response.data.token) {
    localStorage.setItem('token', response.data.token);
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
  localStorage.removeItem('token');
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
    return { valid: false };
  }
};

