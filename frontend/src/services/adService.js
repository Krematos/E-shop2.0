import api from './api';

/**
 * Vytvoření nového inzerátu
 */
export const createAd = async (adData) => {
  const formData = new FormData();
  
  // Přidání textových polí (kromě speciálních polí)
  const { images, mainImageIndex, onProgress, ...textFields } = adData;
  
  Object.keys(textFields).forEach(key => {
    if (textFields[key] !== null && textFields[key] !== undefined) {
      formData.append(key, textFields[key]);
    }
  });
  
  // Přidání obrázků
  if (images && images.length > 0) {
    images.forEach((image) => {
      if (image.file) {
        formData.append('images', image.file);
      }
    });
  }
  
  // Přidání indexu hlavního obrázku
  if (mainImageIndex !== undefined) {
    formData.append('mainImageIndex', mainImageIndex.toString());
  }
  
  const response = await api.post('/ads', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    onUploadProgress: (progressEvent) => {
      if (progressEvent.total && onProgress) {
        const percentCompleted = Math.round(
          (progressEvent.loaded * 100) / progressEvent.total
        );
        onProgress(percentCompleted);
      }
    },
  });
  
  return response.data;
};

/**
 * Získání všech inzerátů
 */
export const getAds = async (params = {}) => {
  const response = await api.get('/ads', { params });
  return response.data;
};

/**
 * Získání inzerátu podle ID
 */
export const getAdById = async (id) => {
  const response = await api.get(`/ads/${id}`);
  return response.data;
};

/**
 * Aktualizace inzerátu
 */
export const updateAd = async (id, adData) => {
  const formData = new FormData();
  
  Object.keys(adData).forEach(key => {
    if (key !== 'images' && key !== 'mainImageIndex') {
      formData.append(key, adData[key]);
    }
  });
  
  if (adData.images && adData.images.length > 0) {
    adData.images.forEach((image) => {
      if (image.file) {
        formData.append('images', image.file);
      }
    });
  }
  
  const response = await api.put(`/ads/${id}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  
  return response.data;
};

/**
 * Smazání inzerátu
 */
export const deleteAd = async (id) => {
  const response = await api.delete(`/ads/${id}`);
  return response.data;
};

