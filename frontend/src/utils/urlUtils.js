
export const getImageUrl = (path) => {
    if (!path) return null;
    if (path.startsWith('http')) return path;

    // Získání base URL z env proměnné nebo fallback
    // Odstraníme případné /api na konci, pokud tam je, protože obrázky jsou na /uploads/
    let baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

    // Pokud VITE_API_BASE_URL obsahuje /api, odstraníme ho pro získání root URL
    if (baseUrl.endsWith('/api')) {
        baseUrl = baseUrl.slice(0, -4);
    }

    // Ošetření dvojitých lomítek
    const cleanBaseUrl = baseUrl.replace(/\/+$/, '');
    const cleanPath = path.replace(/^\/+/, '');

    return `${cleanBaseUrl}/${cleanPath}`;
};
