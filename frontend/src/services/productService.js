import api from './api';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

/**
 * Získání všech produktů
 */
export const getProducts = async (page = 0, size = 12) => {
  const response = await api.get(`/products?page=${page}&size=${size}`);
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
  const formData = new FormData();

  if (productData.name) formData.append('name', productData.name);
  if (productData.description) formData.append('description', productData.description);
  if (productData.price) formData.append('price', productData.price);
  if (productData.category) formData.append('category', productData.category);

  if (productData.images && Array.isArray(productData.images)) {
    productData.images.forEach((imgObj) => {
      if (imgObj.file) {
        formData.append('imagesFilenames', imgObj.file);
      }
    });
  }

  const response = await api.post('/products', formData, {
    withCredentials: true,
    headers: {
      'Content-Type': undefined, //  prohlížeč nastaví správný Content-Type s boundary pro multipart/form-data
    }
  });

  return response.data;
};

/**
 * Aktualizace produktu (pouze ADMIN)
 */
export const updateProduct = async (id, productData) => {
  const formData = new FormData();

  if (productData.name) formData.append('name', productData.name);
  if (productData.description) formData.append('description', productData.description);
  if (productData.price) formData.append('price', productData.price);
  if (productData.category) formData.append('category', productData.category);

  if (productData.images && Array.isArray(productData.images)) {
    productData.images.forEach((imgObj) => {
      if (imgObj.file) {
        formData.append('imagesFilenames', imgObj.file);
      }
    });
  }

  const response = await api.put(`/products/${id}`, formData,{
    headers: {
      'Content-Type': undefined //  prohlížeč nastaví správný Content-Type s boundary pro multipart/form-data
    }
  });
    return response.data;
};

/**
 * Smazání produktu (pouze ADMIN)
 */
export const deleteProduct = async (id) => {
  const response = await api.delete(`/products/${id}`);
  return response.data;
};

