import api from './api';

/**
 * Získání všech produktů
 */
export const getProducts = async () => {
  const response = await api.get('/products');
  return response.data;
};

/**
 * Získání produktu podle ID
 */
export const getProductById = async (id) => {
  const response = await api.get(`/products/${id}`);
  return response.data;
};

/**
 * Vytvoření nového produktu (pouze ADMIN)
 */
export const createProduct = async (productData) => {
  const response = await api.post('/products', productData);
  return response.data;
};

/**
 * Aktualizace produktu (pouze ADMIN)
 */
export const updateProduct = async (id, productData) => {
  const response = await api.put(`/products/${id}`, productData);
  return response.data;
};

/**
 * Smazání produktu (pouze ADMIN)
 */
export const deleteProduct = async (id) => {
  const response = await api.delete(`/products/${id}`);
  return response.data;
};

