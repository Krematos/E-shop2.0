import api from './api';

/**
 * Získání a nastavení CSRF tokenu z endpointu /api/csrf/token.
 * Musí se volat před každou mutací (POST, PUT, DELETE),
 * protože u multipart/form-data axios se nepročte token z cookie automaticky.
 */
const ensureCsrfToken = async () => {
  const response = await api.get('/csrf/token');
  if (response.data?.token) {
    api.defaults.headers.common['X-XSRF-TOKEN'] = response.data.token;
  }
};


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
  await ensureCsrfToken(); // Musíme mít CSRF token před multipart POST

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
    headers: {
      // Nechá browser nastavit Content-Type s boundary pro multipart/form-data automaticky
      'Content-Type': undefined,
    },
  });

  return response.data;
};

/**
 * Aktualizace produktu (pouze ADMIN)
 */
export const updateProduct = async (id, productData) => {
  await ensureCsrfToken(); // Musíme mít CSRF token před multipart PUT

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

  const response = await api.put(`/products/${id}`, formData, {
    headers: {
      // Nechá browser nastavit Content-Type s boundary pro multipart/form-data automaticky
      'Content-Type': undefined,
    },
  });

  return response.data;
};

/**
 * Smazání produktu (pouze ADMIN)
 */
export const deleteProduct = async (id) => {
  await ensureCsrfToken(); // Musíme mít CSRF token před DELETE
  const response = await api.delete(`/products/${id}`);
  return response.data;
};

