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

  // 1. Prepare Product DTO (JSON part)
  const productDto = {
    name: productData.name,
    description: productData.description,
    price: productData.price,
    category: productData.category,
    // Add other fields if necessary
  };

  // Append 'product' part as JSON
  formData.append('product', new Blob([JSON.stringify(productDto)], { type: 'application/json' }));

  // 2. Append Images
  if (productData.images && Array.isArray(productData.images)) {
    productData.images.forEach((imgObj) => {
      if (imgObj.file) {
        formData.append('images', imgObj.file);
      }
    });
  }

  // 3. Send Request
  // Note: We explicitly set Content-Type to multipart/form-data to override the default application/json
  // Axios will automatically add the boundary when it detects FormData
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
        formData.append('images', imgObj.file);
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

