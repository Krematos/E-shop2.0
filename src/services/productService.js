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
  if (productData.stockQuantity !== undefined) formData.append('stockQuantity', productData.stockQuantity);
  if (productData.vatRate !== undefined) formData.append('vatRate', productData.vatRate);

  if (productData.mainImageIndex !== undefined) formData.append('mainImageIndex', productData.mainImageIndex);

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
  const payload = {
    name: productData.name,
    description: productData.description,
    price: productData.price,
    newTotalStock: productData.newTotalStock !== undefined ? productData.newTotalStock : productData.stockQuantity, 
    category: productData.category,
    mainImageIndex: productData.mainImageIndex // Posíláme pouze integer na BE dle instrukcí
  };
  
  const response = await api.put(`/products/${id}`, payload);
  return response.data;
};

/**
 * Nahrání obrázků k existujícímu produktu (pouze ADMIN)
 */
export const uploadProductImages = async (id, images) => {
  const formData = new FormData();
  
  if (Array.isArray(images)) {
    images.forEach((imgObj) => {
      if (imgObj.file) formData.append('image', imgObj.file);
    });
  }

  const response = await api.post(`/products/${id}/images`, formData, MULTIPART_CONFIG);
  return response.data;
};

/**
 * Nahrání nového hlavního obrázku (pouze ADMIN)
 */
export const updateMainImage = async (id, imageFile) => {
  const formData = new FormData();
  formData.append('image', imageFile);
  const response = await api.post(`/products/${id}/main-image`, formData, MULTIPART_CONFIG);
  return response.data;
};

/**
 * Nastavení existujícího obrázku jako hlavního (pouze ADMIN)
 */
export const setExistingImageAsMain = async (productId, imageId) => {
  const response = await api.post(`/products/${productId}/images/${imageId}/set-main`, {}, {
    headers: { 'Content-Type': 'application/json' }
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

/**
 * Smazání obrázku u produktu (pouze ADMIN)
 */
export const deleteProductImage = async (id, filename) => {
  const response = await api.delete(`/products/${id}/images`, {
    params: { filename }
  });
  return response.data;
};
