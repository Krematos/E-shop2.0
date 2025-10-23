import axios from "axios";

// 🔹 Základní konfigurace API klienta
const api = axios.create({
  baseURL: "http://localhost:8080/api", // adresa backendu (přizpůsob dle potřeby)
  headers: {
    "Content-Type": "application/json",
  },
});

// 🔹 Interceptor – přidává JWT token do každého požadavku
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token"); // uložený token po přihlášení
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

//
// ============== AUTHENTIKACE ==============
//

// 🟢 Registrace uživatele
export const register = async (username, email, password) => {
  const response = await api.post("/auth/register", { username, email, password });
  return response.data;
};

// 🟢 Přihlášení uživatele
export const login = async (email, password) => {
  const response = await api.post("/auth/login", { email, password });
  // JWT token uložíme do localStorage
  if (response.data.token) {
    localStorage.setItem("token", response.data.token);
  }
  return response.data;
};

// 🟢 Odhlášení uživatele
export const logout = () => {
  localStorage.removeItem("token");
};

//
// ============== UŽIVATELSKÉ FUNKCE ==============
//

// 🟢 Získání informací o přihlášeném uživateli
export const getCurrentUser = async () => {
  const response = await api.get("/users/me");
  return response.data;
};

// 🟢 Načtení všech produktů (např. na hlavní stránce)
export const getProducts = async () => {
  const response = await api.get("/products");
  return response.data;
};

//
// ============== EXPORT INSTANCE ==============
export default api;
