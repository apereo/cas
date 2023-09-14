import forIn from 'lodash/forIn';
import isObject from 'lodash/isObject';
import merge from 'lodash/merge';
import has from 'lodash/has';

const isRemoteRef = (ref) => false;

const encodeToken = (pointer) => {
    return pointer
        .replace(new RegExp('~', 'g'), '~0')
        .replace(new RegExp('/', 'g'), '~1');
};
const decodeToken = (pointer) => {
    return pointer
        .replace(new RegExp('~1', 'g'), '/')
        .replace(new RegExp('~0', 'g'), '~');
};

export const get = (schema, pointer) => {

    if (!isPointer(pointer)) {
        throw new Error(`invalid JSON pointer specified: '${pointer}'`);
    }
    const fragments = pointer.split('/');
    return fragments.reduce((object, fragment) => {
        if (fragment === '#' || fragment === '/' || fragment === '') {
            return object;
        }
        const token = fragment.replace('~1', '/').replace('~0', '~');
        let reference = null;

        if (Array.isArray(object)) {
            const index = parseInt(token, 10);

            if (!object.indexOf(index)) {
                throw new Error(
                    `could not dereference JSON pointer: ${pointer}. Array does not have`
                    + ` index: ${index}::${JSON.stringify(object)}`);
            }

            reference = object[index];
        } else {
            if (!has(object, token)) {
                throw new Error(
                    `could not dereference pointer '${pointer}'. The fragment ${token}`
                    + ` is not a valid property of object: ${JSON.stringify(object, null, 2)}`);
            }
            reference = object[token];
        }
        return reference;
    }, schema);
};

export const set = (obj, pointer, value) => {

    if (!isPointer(pointer)) {
        throw new Error(`invalid JSON pointer specified: '${pointer}'`);
    }
    const fragments = pointer.split('/');
    let ref = obj;
    return fragments.forEach((fragment, index) => {
        if (fragment === '#' || fragment === '/' || fragment === '') {
            return;
        }
        const token = decodeToken(fragment);
        if (Array.isArray(ref)) {
            const i = parseInt(token, 10);

            if (!ref.indexOf(i)) {
                throw new Error(
                    `could not set JSON pointer: ${pointer}. Array does not have`
                    + ` index: ${index}::${JSON.stringify(obj)}`);
            }

            if ((index + 1) === fragments.length) {
                ref[i] = value;
                return;
            }

            ref = ref[i];
        } else {
            if (!has(ref, token)) {
                throw new Error(
                    `could not set pointer: '${pointer}'. The fragment ${token}`
                    + ` is not a valid property of object: ${JSON.stringify(obj, null, 2)}`);
            }

            if ((index + 1) === fragments.length) {
                ref[token] = value;
                return;
            }
            ref = ref[token];
        }
    });
};


export const isPointer = (input) => {
    if (typeof input !== 'string') {
        return false;
    }

    if (input === '') {
        return true;
    }

    if (/^#|^\//.test(input)) {
        return true;
    }
    return false;
};

function isCyclic(obj) {
    var keys = [];
    var stack = [];
    var stackSet = new Set();
    var detected = false;

    function detect(obj, key) {
        if (typeof obj != 'object') { return; }

        if (stackSet.has(obj)) { // it's cyclic! Print the object and its locations.
            var oldindex = stack.indexOf(obj);
            var l1 = keys.join('.') + '.' + key;
            var l2 = keys.slice(0, oldindex + 1).join('.');
            console.log('CIRCULAR: ' + l1 + ' = ' + l2 + ' = ' + obj);
            console.log(obj);
            detected = true;
            return;
        }

        keys.push(key);
        stack.push(obj);
        stackSet.add(obj);
        for (var k in obj) { //dive on the object's children
            if (obj.hasOwnProperty(k)) { detect(obj[k], k); }
        }

        keys.pop();
        stack.pop();
        stackSet.delete(obj);
        return;
    }

    detect(obj, 'obj');
    return detected;
}

export const dereference = (root, resolver) => {
    // ### JSON In, JSON Out
    //
    // The [json specification](http://www.ietf.org/rfc/rfc4627.txt) section 2.1
    // states:

    // >  A JSON value MUST be an object, array, number, or string, or one of
    // >  the following three literal names: false null true

    // Any other value should result in an `TypeError` being thrown.

    if (!(typeof root).match(/object|string|number|boolean/)) {
        throw new TypeError(
            `@jst/dereference: argument not a valid json value: ${typeof root} | ${root}`);
    }
    const circularRefs = {};

    const walk = (schema, resolve = null, path = '#') => {
        // If schema is an array we dereference each schema and then merge them from
        // right-to-left.
        if (Array.isArray(schema)) {
            // first validate our arguments assumption!
            schema.forEach((s) => {
                if (typeof s !== 'object' && !Array.isArray(s)) {
                    throw new TypeError(`expect typeof object got: ${typeof s}`);
                }
            });

            // then dereference each schema in the array before eventually merging them
            // from right to left using a reducer function.
            return schema
                .map((scm, index) => walk(scm, resolve, `${path}/${index}`))
                .reduce((acc, scm) => merge(acc, scm), {});

            // If schema is not an array of json objects we expect a singlular json schema
            // be provided
        } else if (isObject(schema)) {
            const schemaId = schema.id || undefined;
            let isCircular = false;

            // traverse is an internal recursive function that we bind to this lexical
            // scope in order to easily resolve to schema definitions whilst traversing
            // an objects nested properties. This is primarily for efficiency concerns.
            const traverse = (node, nodePath = '#') => {
                let resolution = {};

                if (typeof node !== 'object' || node === null) {
                    return node;
                }

                // if only one argument is provided and it is an array we must recursively
                // dereference it's individual values
                if (Array.isArray(node)) {
                    return node.map((v, index) => traverse(v, `${nodePath}/${index}`));
                }

                // if we are here, the first argument is not an array or value and we expect
                // it to be a json schema.

                forIn(node, (value, key) => {
                    // Skip the following properties
                    if (key === 'definitions') {
                        return;
                    }

                    // If value is not an array, object, or JSON schema reference we can
                    // dereference it immediately. 'typeof array' equals 'object' in JS.
                    if (typeof value !== 'object' && key !== '$ref') {
                        resolution[key] = value;

                        // If we have a schema reference we must fetch it, dereference it, then merge
                        // it into the base schema object.
                    } else if (key === '$ref') {
                        let reference = null;

                        if (isRemoteRef(value)) {
                            if (!resolve) {
                                throw new TypeError(
                                    'argument: resolver is required to dereference a json uri.');
                            }

                            if (value !== schemaId) {
                                reference = resolve(value);

                                if (!reference) {
                                    throw new Error(`unable to resolve URI reference: ${value}`);
                                }

                                resolution = merge(
                                    resolution,
                                    walk(reference, resolve, `${nodePath}/${encodeToken(key)}`),
                                    true,
                                );
                            } else {
                                reference = resolution;
                                circularRefs[nodePath] = schema;
                                isCircular = true;
                            }

                            // de-reference a json pointer
                        } else if (isPointer(value)) {
                            reference = get(schema, value);
                            console.log(reference, value);
                            /*resolution = merge(
                                resolution,
                                traverse(reference, `${nodePath}/${encodeToken(key)}`),
                            );*/
                        } else {
                            throw new Error(
                                `could not dereference value as a json pointer or uri: ${value}`);
                        }

                        if (!reference) {
                            throw new ReferenceError(`could not find a reference to ${value}`);
                        }

                        // Otherwise the value is an array or object and we need to traverse it
                        // and dereference it's properties.
                    } else {
                        resolution[key] = traverse(value, `${nodePath}/${encodeToken(key)}`);
                    }
                });


                return resolution;
            };

            return traverse(schema, path);

            // if any other combination of arguments is provided we throw
        } else {
            throw new TypeError(`expected first parameter to be object or array: ${schema}`);
        }
    };

    const result = walk(root, resolver);

    // We can now handle any circular references in the schema by iterating our
    // store of circular references encountered whilst processing the schema. We will
    // only dereference a circular schema once, I could write a monologue about
    // this topic but let it suffice to say JST does not make the decision what is
    // the correct amount of _circular depth_ to dereference, we only do so
    // once. Users can simply call `dereference` again with the resultant schema
    // to get another level of nesting.
    forIn(circularRefs, (value, key) => {
        set(result, key.split('$ref/').join(''), value);
    });

    return result;
};