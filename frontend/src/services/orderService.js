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
export const getAllOrders = async () => {
  const response = await api.get('/orders/all');
  return response.data;
};

/**
 * Získání objednávky podle ID
 */
export const getOrderById = async (orderId) => {
  const response = await api.get(`/orders/${orderId}`);
  return response.data;
};

