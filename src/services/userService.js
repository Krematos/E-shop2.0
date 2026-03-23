import api from './api';

/**
 * Získání informací o přihlášeném uživateli.
 * Chyby (vč. 401) se propagují – 401 zachytí interceptor v api.js a přesměruje na /login.
 */
export const getCurrentUser = async () => {
  const response = await api.get('/user/me');
  return response.data;
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

/**
 * Smazání vlastního účtu přihlášeného uživatele
 */
export const deleteAccount = async () => {
  const response = await api.delete('/user/me');
  return response.data;
};
