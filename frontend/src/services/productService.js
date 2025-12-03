import api from './api';

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

  // 1. Append flat fields (backend uses @ModelAttribute)
  if (productData.name) formData.append('name', productData.name);
  if (productData.description) formData.append('description', productData.description);
  if (productData.price) formData.append('price', productData.price);
  if (productData.category) formData.append('category', productData.category);
  // Add other fields if necessary

  // 2. Append Images
  // Backend expects 'imagesFilenames' as the key for the list of files
  if (productData.images && Array.isArray(productData.images)) {
    productData.images.forEach((imgObj) => {
      if (imgObj.file) {
        formData.append('imagesFilenames', imgObj.file);
      }
    });
  }

  // 3. Send Request
  const response = await api.post('/products', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return response.data;
};

/**
 * Aktualizace produktu (pouze ADMIN)
 */
export const updateProduct = async (id, productData) => {
  const formData = new FormData();

  // Flat fields for @ModelAttribute
  if (productData.name) formData.append('name', productData.name);
  if (productData.description) formData.append('description', productData.description);
  if (productData.price) formData.append('price', productData.price);
  if (productData.category) formData.append('category', productData.category);
  // Add other fields if necessary

  // Append Images
  if (productData.images && Array.isArray(productData.images)) {
    productData.images.forEach((imgObj) => {
      if (imgObj.file) {
        formData.append('imagesFilenames', imgObj.file);
      }
    });
  }

  const response = await api.put(`/products/${id}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
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

