import { createContext, useContext, useState, useEffect } from 'react';
import { login as loginService, logout as logoutService, validateToken } from '../services/authService';

const AuthContext = createContext(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Načtení uživatele z localStorage při startu
    // Token je v HttpOnly cookie, není přístupný přes JavaScript
    const userStr = localStorage.getItem('user');

    if (userStr) {
      try {
        const userData = JSON.parse(userStr);
        setUser(userData);
        // Ověření tokenu (token je v cookie)
        validateToken().then((result) => {
          if (!result.valid) {
            logout();
          }
        });
      } catch (error) {
        console.error('Chyba při načítání uživatele:', error);
        logout();
      }
    }
    setLoading(false);
  }, []);

  const login = async (username, password) => {
    try {
      const response = await loginService(username, password);
      const userData = {
        username: response.username,
        roles: response.roles,
      };
      setUser(userData);
      return response;
    } catch (error) {
      throw error;
    }
  };

  const logout = () => {
    logoutService();
    setUser(null);
  };

  const isAdmin = () => {
    if (!user || !user.roles) return false;
    return user.roles.some(
      (role) => role.authority === 'ROLE_ADMIN' || role === 'ROLE_ADMIN'
    );
  };

  const isAuthenticated = () => {
    // Ověření autentizace na základě přítomnosti user dat
    // Token je v HttpOnly cookie a není přístupný přes JavaScript
    return !!user;
  };

  const value = {
    user,
    login,
    logout,
    isAdmin,
    isAuthenticated,
    loading,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

