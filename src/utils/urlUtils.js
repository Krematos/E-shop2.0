/**
 * Základní URL pro obrázky.
 * * Zkouší načíst hodnotu z .env souboru (pro produkci/konfiguraci).
 * Pokud není definována, použije fallback na localhost (pro vývoj).
 */
export const IMAGE_BASE_URL = import.meta.env.VITE_IMAGE_BASE_URL || 'http://localhost:8080/api/images/';

/**
 * Vygeneruje kompletní URL pro obrázek.
 * * @param {string|null} filename - Název souboru z databáze (např. "uuid.jpg")
 * @returns {string|null} - Kompletní URL nebo null
 */
export const getImageUrl = (filename) => {
  // Ochrana proti null/undefined/prázdným stringům
  if (!filename || typeof filename !== 'string') {
    return null;
  }

  // Podpora pro stará data (pokud už je v DB celá URL)
  // Toto zabrání vytvoření nesmyslu typu "http://localhost.../http://..."
  if (filename.startsWith('http://') || filename.startsWith('https://')) {
    return filename;
  }

  // Sestavení finální URL
  // Ošetření, aby nevzniklo dvojité lomítko (např. ...uploads//obrazek.jpg)
  const cleanBase = IMAGE_BASE_URL.endsWith('/')
    ? IMAGE_BASE_URL
    : `${IMAGE_BASE_URL}/`;

  const cleanFilename = filename.startsWith('/')
    ? filename.substring(1)
    : filename;

  return `${cleanBase}${cleanFilename}`;
};

/**
 * Vygeneruje srcset atribut pro responsivní obrázky.
 * Nabízí různé varianty velikostí pro lepší LCP na mobilních zařízeních.
 */
export const getResponsiveSrcSet = (filename) => {
  const base = getImageUrl(filename);
  if (!base) return undefined;

  // Podpora jen pro API (pro externí URL necháme beze změny)
  if (base.startsWith(IMAGE_BASE_URL)) {
    return `${base}?w=330 330w, ${base}?w=660 660w, ${base}?w=1200 1200w`;
  }

  return undefined; // Pro externí URL nepoužíváme srcset z API serveru
};

/**
 * Výchozí pravidla pro atribut sizes.
 * Na mobilu zobrazeno na 100vw, na desktopu max 330px šířky (odpovídá kontejneru 440x330).
 */
export const defaultImageSizes = "(max-width: 768px) 100vw, 330px";

/**
 * Převede libovolný text na čistý URL slug (odstraní diakritiku, převede na malá písmena, nahradí mezery pomlčkami).
 */
export const slugify = (text) => {
  if (!text) return '';
  return text
    .toString()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '');
};

/**
 * Sestaví SEO-friendly URL pro produkt ve tvaru /products/:slug-:id.
 * Pokud produkt nemá název, vrátí fallback /products/:id.
 */
export const getProductUrl = (product) => {
  if (!product) return '/products';
  const id = product.id;
  const slug = slugify(product.name);
  return slug ? `/products/${slug}-${id}` : `/products/${id}`;
};

/**
 * Extrahuje číselné ID produktu z parametru URL (podporuje jak "42", tak "sluchatka-sony-wh-1000xm4-42").
 */
export const extractProductId = (param) => {
  if (!param) return null;
  const match = String(param).match(/-?(\d+)$/);
  return match ? match[1] : param;
};
