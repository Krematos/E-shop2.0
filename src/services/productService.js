import api from './api';

// ── Helper ──────────────────────────────────────────────────────────────────

/**
 * Sestaví FormData z dat produktu – sdílená logika pro create i update.
 * @param {object} productData
 * @returns {FormData}
 */
const buildProductFormData = (productData) => {
  const formData = new FormData();

  if (productData.name)        formData.append('name', productData.name);
  if (productData.description) formData.append('description', productData.description);
  if (productData.price)       formData.append('price', productData.price);
  if (productData.category)    formData.append('category', productData.category);

  if (Array.isArray(productData.images)) {
    productData.images.forEach((imgObj) => {
      if (imgObj.file) formData.append('imagesFilenames', imgObj.file);
    });
  }

  return formData;
};

// Multipart requesty vyžadují, aby browser sám nastavil Content-Type s boundary
const MULTIPART_CONFIG = { headers: { 'Content-Type': undefined } };

// ── API funkce ───────────────────────────────────────────────────────────────

/**
 * Získání všech produktů (stránkování)
 */
export const getProducts = async (page = 0, size = 12) => {
  const response = await api.get('/products', { params: { page, size } });
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
  const response = await api.post('/products', buildProductFormData(productData), MULTIPART_CONFIG);
  return response.data;
};

/**
 * Aktualizace produktu (pouze ADMIN)
 */
export const updateProduct = async (id, productData) => {
  const response = await api.put(`/products/${id}`, buildProductFormData(productData), MULTIPART_CONFIG);
  return response.data;
};

/**
 * Smazání produktu (pouze ADMIN)
 */
export const deleteProduct = async (id) => {
  const response = await api.delete(`/products/${id}`);
  return response.data;
};
