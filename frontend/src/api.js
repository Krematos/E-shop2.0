import axios from "axios";

// 🌍 Základní URL backendu
const API_URL = "http://localhost:8080/api";

// 🔐 Funkce pro přihlášení uživatele
export const login = async (email, password) => {
  try {
    const response = await axios.post(`${API_URL}/login`, {
      email,
      password,
    });

    // backend by měl vrátit JWT token — např. { token: "...", user: {...} }
    const data = response.data;

    // uložíme token do localStorage
    if (data.token) {
      localStorage.setItem("token", data.token);
    }

    return data;
  } catch (error) {
    console.error("Chyba při přihlášení:", error.response?.data || error.message);
    throw error;
  }
};

// 🧾 Funkce pro registraci nového uživatele
export const register = async (username, email, password) => {
  try {
    const response = await axios.post(`${API_URL}/register`, {
      username,
      email,
      password,
    });
    return response.data;
  } catch (error) {
    console.error("Chyba při registraci:", error.response?.data || error.message);
    throw error;
  }
};

// 🔒 Axios instance s tokenem pro chráněné endpointy
export const authApi = axios.create({
  baseURL: API_URL,
});

// interceptor pro automatické přidání tokenu do hlavičky
authApi.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});