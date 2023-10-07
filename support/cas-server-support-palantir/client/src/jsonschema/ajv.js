import Ajv from 'ajv/dist/2020';
import addFormats from 'ajv-formats';

export const createAjv = (options = {}) => {
  const ajv = new Ajv({
    allErrors: true,
    verbose: true,
    strict: false,
    ...options,
  });
  addFormats(ajv);
  return ajv;
};