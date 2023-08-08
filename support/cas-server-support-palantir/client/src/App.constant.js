/*eslint-disable no-restricted-globals*/
const url = `${location.origin}`;
const APP_ORIGIN = url;

const API_PATH = import.meta.env.API_PATH || "/";
const APP_PATH = url || import.meta.env.APP_PATH || "http://localhost:3000";

export { API_PATH, APP_PATH, APP_ORIGIN };
