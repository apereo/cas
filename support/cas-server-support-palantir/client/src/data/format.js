import { cloneDeep, isArray, isObject, omit } from 'lodash';
import { removeNull } from './removeNull';

const types = {
    HASH: "java.util.HashSet",
    LIST: "java.util.ArrayList",
};

export const formatToSave = (body) => {
    const cloned = cloneDeep(body);
    const clone = Object.keys(cloned).reduce((p, key, idx, list) => {
        let value = cloned[key];
        if (isArray(value)) {
            if (value.length > 0) {
                const obj = isObject(value[0]);
                value = [
                    obj ? types.LIST : types.HASH,
                    obj ? value.map(v => formatToSave(v)) : value
                ];
            } else {
                value = null;
            }
        } else if (isObject(value)) {
            value = Object.keys(omit(value, ['@class'])).length > 0 ? formatToSave(value) : null;
        }
        return {
            ...p,
            [key]: value
        };
    }, {});
    return removeNull(clone);
};

export const formatToRead = (body) => {
    const cloned = cloneDeep(body);
    const clone = Object.keys(cloned).reduce((p, key, idx, list) => {
        let value = cloned[key];
        if (isArray(cloned[key])) {
            value = value[1].map(v => isObject(v) ? formatToRead(v) : v);
        } else if (isObject(cloned[key])) {
            value = formatToRead(cloned[key]);
        }
        return !value ? p : {
            ...p,
            [key]: value
        };
    }, {});
    return clone;
};