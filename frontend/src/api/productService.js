const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api/products";

/**
 * Pomocná funkce pro získání tokenu.
 * Uprav si klíč 'token' nebo 'jwt' podle toho, jak ho ukládáš v localStorage.
 */
const getAuthHeader = (token) => {
  const storedToken = token || localStorage.getItem("token");
  if (storedToken) {
    return { Authorization: `Bearer ${storedToken}` };
  }
  return {};
};

/**
 * Pomocná funkce pro přípravu FormData.
 * Převede JS objekt na formát, který umí přenášet soubory.
 */
const createFormData = (productData) => {
  const formData = new FormData();

  // 1. Textová data
  // Musíme ošetřit null/undefined hodnoty
  if (productData.name) formData.append("name", productData.name);
    if (productData.description) formData.append("description", productData.description);
  if (productData.price) formData.append("price", productData.price);
  if (productData.category) formData.append("category", productData.category);

  // 2. Obrázky
  // React komponenta posílá pole objektů { file: File, preview: "...", ... }
  if (productData.images && Array.isArray(productData.images)) {
    productData.images.forEach((imageObj) => {
      // Pokud je to nový obrázek k nahrání (má vlastnost file)
      if (imageObj.file) {
        formData.append("images", imageObj.file);
      }
    });
  }

  return formData;
};

// --- API METODY ---

export const getProducts = async (page = 0, size = 10) => {
  const response = await fetch(`${API_URL}?page=${page}&size=${size}`);
  if (!response.ok) {
    throw new Error("Chyba při načítání produktů");
  }
  return await response.json();
};

export const getProductById = async (id) => {
  const response = await fetch(`${API_URL}/${id}`);
  if (!response.ok) {
    throw new Error("Produkt nenalezen");
  }
  return await response.json();
};

export const createProduct = async (productData, token) => {
  const formData = createFormData(productData);

  const response = await fetch(API_URL, { // Endpoint pro vytvoření
    method: "POST",
    headers: {
      ...getAuthHeader(token),
    },
    body: formData,
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Chyba při vytváření produktu: ${errorText}`);
  }

  return await response.json();
};

export const updateProduct = async (id, productData, token) => {
  const formData = createFormData(productData);

  const response = await fetch(`${API_URL}/${id}`, {
    method: "PUT",
    headers: {
      ...getAuthHeader(token),
      // Opět: Content-Type nenastavujeme ručně
    },
    body: formData,
  });

  if (!response.ok) {
    throw new Error("Chyba při aktualizaci produktu");
  }

  return await response.json();
};

export const deleteProduct = async (id, token) => {
  const response = await fetch(`${API_URL}/${id}`, {
    method: "DELETE",
    headers: {
      ...getAuthHeader(token),
    },
  });

  if (!response.ok) {
    throw new Error("Chyba při mazání produktu");
  }

  return response.status === 204;
};