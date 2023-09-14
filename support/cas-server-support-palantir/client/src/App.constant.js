/*eslint-disable no-restricted-globals*/
export const getBasePath = () => {
    const base = document.getElementsByTagName('base');
    let url = '';
    if (base) {
        url = new URL(base[0].href);
        return url.pathname?.replace(/^\/+/g, '');
    }

    return '';
};
export const BASE_PATH = getBasePath();

const url = `${location.origin}`;
const APP_ORIGIN = url;

const API_PATH = `/${BASE_PATH.replace('/dashboard', import.meta.env.VITE_API_PATH || '')}`;
const APP_PATH = BASE_PATH || import.meta.env.VITE_APP_PATH;

export { API_PATH, APP_PATH, APP_ORIGIN };
