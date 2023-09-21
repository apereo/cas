export function checkByType(value) {
    switch (typeof value) {
        case 'object': {
            return Object.keys(value).filter(k => !!value[k]).length > 0;
        }
        default: {
            return true;
        }
    }
}

export function removeNull(attribute, discardObjects = false) {
    if (!attribute) { return {}; }
    let removed = Object.keys(attribute).reduce((coll, val, index) => {
        if (attribute[val] !== null) {
            if (!discardObjects || checkByType(attribute[val])) {
                coll[val] = attribute[val];
            }
        }
        return coll;
    }, {});
    return removed;
}