import api from './api';

/**
 * Získání informací o přihlášeném uživateli
 */
export const getCurrentUser = async () => {
  try {
    const response = await api.get('/user/me');
    return response.data;
  } catch (error) {
    console.error('Chyba při načítání uživatele:', error);
    // Fallback na localStorage pokud API selže
    const userStr = localStorage.getItem('user');
    if (userStr) {
      return JSON.parse(userStr);
    }
    return null;
  }
};

/**
 * Získání uživatele podle ID
 */
export const getUserById = async (userId) => {
  const response = await api.get(`/user/${userId}`);
  return response.data;
};

/**
 * Aktualizace uživatele
 */
export const updateUser = async (userId, userData) => {
  const response = await api.put(`/user/${userId}`, userData);
  return response.data;
};

