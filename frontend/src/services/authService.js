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

  // Token je nyní v HttpOnly cookie, ukládáme pouze user data
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
  // Token je v HttpOnly cookie a bude smazán backendem
  // Mažeme pouze user data z localStorage
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

/**
    * Získání role uživatele
    */
export const getUserRole = async () => {
  // Token je v HttpOnly cookie, není potřeba ho manuálně přidávat
  try {
    const response = await api.get('/auth/role');
    return response.data.role;
  } catch (error) {
    console.error('Error fetching user role:', error);
    throw error;
  }
};

export const requestPasswordReset = async (email) => {
  const response = await api.post('/auth/forgot-password', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email }),
  });

  if (!response.ok) {
    // Můžeš zkusit přečíst chybovou zprávu ze serveru
    const errorText = await response.text();
    throw new Error(errorText || 'Chyba při žádosti o obnovu hesla');
  }


  return true;
};

export default {
  register,
  login,
  logout,
  validateToken,
  getUserRole,
};

