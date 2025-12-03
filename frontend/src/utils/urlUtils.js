/**
 * Základní URL pro obrázky.
 * * 1. Zkouší načíst hodnotu z .env souboru (pro produkci/konfiguraci).
 * 2. Pokud není definována, použije fallback na localhost (pro vývoj).
 */
export const IMAGE_BASE_URL = import.meta.env.VITE_IMAGE_BASE_URL || 'http://localhost:8080/uploads/';

/**
 * Vygeneruje kompletní URL pro obrázek.
 * * @param {string|null} filename - Název souboru z databáze (např. "uuid.jpg")
 * @returns {string|null} - Kompletní URL nebo null
 */
export const getImageUrl = (filename) => {
  // 1. Ochrana proti null/undefined/prázdným stringům
  if (!filename || typeof filename !== 'string') {
    return null; // Nebo vraťte cestu k placeholderu: '/assets/placeholder.png'
  }

  // 2. Podpora pro stará data (pokud už je v DB celá URL)
  // Toto zabrání vytvoření nesmyslu typu "http://localhost.../http://..."
  if (filename.startsWith('http://') || filename.startsWith('https://')) {
    return filename;
  }

  // 3. Sestavení finální URL
  // Ošetření, aby nevzniklo dvojité lomítko (např. ...uploads//obrazek.jpg)
  const cleanBase = IMAGE_BASE_URL.endsWith('/')
    ? IMAGE_BASE_URL
    : `${IMAGE_BASE_URL}/`;

  const cleanFilename = filename.startsWith('/')
    ? filename.substring(1)
    : filename;

  return `${cleanBase}${cleanFilename}`;
};
