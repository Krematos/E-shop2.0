const API_URL = "http://localhost:8080/api/products";

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
  if (productData.title) formData.append("name", productData.title); // Fallback pro různé názvy
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
      // Pokud potřebuješ řešit i zachování starých obrázků,
      // musel bys sem přidat logiku pro odeslání jejich URL,
      // ale pro základní nahrávání stačí poslat nové soubory.
    });
  }

  return formData;
};

// --- API METODY ---

export const getProducts = async () => {
  const response = await fetch(API_URL);
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

  const response = await fetch(`${API_URL}/ads`, { // Endpoint pro vytvoření (podle tvého Controlleru)
    method: "POST",
    headers: {
      ...getAuthHeader(token),
      // DŮLEŽITÉ: U FormData NIKDY nenastavuj 'Content-Type': 'multipart/form-data' ručně!
      // Prohlížeč to udělá sám a přidá správné "boundary".
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
      "Content-Type": "application/json",
    },
  });

  if (!response.ok) {
    throw new Error("Chyba při mazání produktu");
  }

  // Delete často vrací 204 No Content (bez těla), takže nevoláme .json() pokud je status 204
  if (response.status === 204) {
      return true;
  }
  return true;
};