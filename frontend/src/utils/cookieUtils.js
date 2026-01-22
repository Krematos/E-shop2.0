/**
 * Utility funkce pro práci s cookies
 */

/**
 * Získá hodnotu cookie podle jména
 * @param {string} name - Název cookie
 * @returns {string|null} Hodnota cookie nebo null pokud neexistuje
 */
export const getCookie = (name) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
        return parts.pop().split(';').shift();
    }
    return null;
};

/**
 * Nastaví cookie
 * @param {string} name - Název cookie
 * @param {string} value - Hodnota cookie
 * @param {Object} options - Volitelné parametry (path, maxAge, secure, sameSite)
 */
export const setCookie = (name, value, options = {}) => {
    let cookieString = `${name}=${value}`;

    if (options.path) {
        cookieString += `; path=${options.path}`;
    }

    if (options.maxAge) {
        cookieString += `; max-age=${options.maxAge}`;
    }

    if (options.secure) {
        cookieString += '; secure';
    }

    if (options.sameSite) {
        cookieString += `; samesite=${options.sameSite}`;
    }

    document.cookie = cookieString;
};

/**
 * Smaže cookie
 * @param {string} name - Název cookie
 * @param {string} path - Cesta cookie (musí odpovídat cestě při vytvoření)
 */
export const deleteCookie = (name, path = '/') => {
    document.cookie = `${name}=; path=${path}; max-age=0`;
};

export default {
    getCookie,
    setCookie,
    deleteCookie,
};
