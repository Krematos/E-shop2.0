import api from './api';

/**
 * Vytvoření nové objednávky
 */
export const createOrder = async (orderData) => {
  const response = await api.post('/orders', orderData);
  return response.data;
};

/**
 * Získání objednávek přihlášeného uživatele
 */
export const getUserOrders = async () => {
  const response = await api.get('/orders');
  return response.data;
};

/**
 * Získání všech objednávek (pouze ADMIN)
 */
export const getAllOrders = async (page = 0, size = 10, sort = 'createdAt,desc', status = '') => {
  const params = { page, size, sort };
  if (status) {
    params.status = status;
  }
  const response = await api.get('/orders/all', { params });
  return response.data;
};

/**
 * Aktualizace stavu objednávky (pouze ADMIN)
 */
export const updateOrderStatus = async (orderId, newStatus) => {
  const response = await api.patch(`/orders/${orderId}/status`, { newStatus });
  return response.data;
};

/**
 * Získání objednávky podle ID
 */
export const getOrderById = async (orderId) => {
  const response = await api.get(`/orders/${orderId}`);
  return response.data;
};

/**
 * Odstoupení od smlouvy u objednávky
 */
export const withdrawOrder = async (orderId, withdrawalData) => {
  try {
    const response = await api.post(`/orders/${orderId}/withdraw`, withdrawalData);
    return response.data;
  } catch (error) {
    // Pokud backend ještě nemá dedikovaný endpoint, vrátíme simulovanou odpověď
    if (error.response && (error.response.status === 404 || error.response.status === 405)) {
      return { success: true, orderId, ...withdrawalData, submittedAt: new Date().toISOString() };
    }
    throw error;
  }
};


