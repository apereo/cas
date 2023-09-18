import _ from 'lodash';

function makeAbsolute (location) {
    return location;
}

export function resolveRefs(obj, options) {
    var allTasks = Promise.resolve();

    allTasks = allTasks
        .then(function () {
            // Validate the provided document
            if (!_.isArray(obj) && !_.isObject(obj)) {
                throw new TypeError('obj must be an Array or an Object');
            }

            // Validate options
            options = validateOptions(options, obj);

            // Clone the input so we do not alter it
            obj = _.cloneDeep(obj);
        })
        .then(function () {
            var metadata = {
                deps: {}, // To avoid processing the same refernece twice, and for circular reference identification
                docs: {}, // Cache to avoid processing the same document more than once
                refs: {} // Reference locations and their metadata
            };

            return buildRefModel(obj, options, metadata)
                .then(function () {
                    return metadata;
                });
        })
        .then(function (results) {
            var allRefs = {};
            var circularPaths = [];
            var circulars = [];
            var depGraph = new gl.Graph();
            var fullLocation = makeAbsolute(options.location);
            var refsRoot = fullLocation + pathToPtr(options.subDocPath);
            var relativeBase = path.dirname(fullLocation);

            // Identify circulars

            // Add nodes first
            Object.keys(results.deps).forEach(function (node) {
                depGraph.setNode(node);
            });

            // Add edges
            _.forOwn(results.deps, function (props, node) {
                _.forOwn(props, function (dep) {
                    depGraph.setEdge(node, dep);
                });
            });

            circularPaths = gl.alg.findCycles(depGraph);

            // Create a unique list of circulars
            circularPaths.forEach(function (path) {
                path.forEach(function (seg) {
                    if (circulars.indexOf(seg) === -1) {
                        circulars.push(seg);
                    }
                });
            });

            // Identify circulars
            _.forOwn(results.deps, function (props, node) {
                _.forOwn(props, function (dep, prop) {
                    var isCircular = false;
                    var refPtr = node + prop.slice(1);
                    var refDetails = results.refs[node + prop.slice(1)];
                    var pathIndex;

                    if (circulars.indexOf(dep) > -1) {
                        // Figure out if the circular is part of a circular chain or just a reference to a circular
                        circularPaths.forEach(function (path) {
                            // Short circuit
                            if (isCircular) {
                                return;
                            }

                            pathIndex = path.indexOf(dep);

                            if (pathIndex > -1) {
                                // Check each path segment to see if the reference location is beneath one of its segments
                                path.forEach(function (seg) {
                                    // Short circuit
                                    if (isCircular) {
                                        return;
                                    }

                                    if (refPtr.indexOf(seg + '/') === 0) {
                                        // If the reference is local, mark it as circular but if it's a remote reference, only mark it
                                        // circular if the matching path is the last path segment or its match is not to a document root
                                        circular = true;
                                    }
                                });
                            }
                        });
                    }

                    if (isCircular) {
                        // Update all references and reference details
                        refDetails.circular = true;
                    }
                });
            });

            // Resolve the references in reverse order since the current order is top-down
            _.forOwn(Object.keys(results.deps).reverse(), function (parentPtr) {
                var deps = results.deps[parentPtr];
                var pPtrParts = parentPtr.split('#');
                var pDocument = results.docs[pPtrParts[0]];
                var pPtrPath = pathFromPtr(pPtrParts[1]);

                _.forOwn(deps, function (dep, prop) {
                    var depParts = splitFragment(dep);
                    var dDocument = results.docs[depParts[0]];
                    var dPtrPath = pPtrPath.concat(pathFromPtr(prop));
                    var refDetails = results.refs[pPtrParts[0] + pathToPtr(dPtrPath)];

                    // Resolve reference if valid
                    if (_.isUndefined(refDetails.error) && _.isUndefined(refDetails.missing)) {
                        if (!options.resolveCirculars && refDetails.circular) {
                            refDetails.value = _.cloneDeep(refDetails.def);
                        } else {
                            try {
                                refDetails.value = findValue(dDocument, pathFromPtr(depParts[1]));
                            } catch (err) {
                                markMissing(refDetails, err);

                                return;
                            }

                            // If the reference is at the root of the document, replace the document in the cache.  Otherwise, replace
                            // the value in the appropriate location in the document cache.
                            if (pPtrParts[1] === '' && prop === '#') {
                                results.docs[pPtrParts[0]] = refDetails.value;
                            } else {
                                setValue(pDocument, dPtrPath, refDetails.value);
                            }
                        }
                    }
                });
            });

            function walkRefs(root, refPtr, refPath) {
                var refPtrParts = refPtr.split('#');
                var refDetails = results.refs[refPtr];
                var refDeps;

                // Record the reference (relative to the root document unless the reference is in the root document)
                allRefs[refPtrParts[0] === options.location ?
                    '#' + refPtrParts[1] :
                    pathToPtr(options.subDocPath.concat(refPath))] = refDetails;

                // Do not walk invalid references
                if (refDetails.circular || !isValid(refDetails)) {
                    // Sanitize errors
                    if (!refDetails.circular && refDetails.error) {
                        // The way we use findRefs now results in an error that doesn't match the expectation
                        refDetails.error = refDetails.error.replace('options.subDocPath', 'JSON Pointer');

                        // Update the error to use the appropriate JSON Pointer
                        if (refDetails.error.indexOf('#') > -1) {
                            refDetails.error = refDetails.error.replace(refDetails.uri.substr(refDetails.uri.indexOf('#')),
                                refDetails.uri);
                        }

                        // Report errors opening files as JSON Pointer errors
                        if (refDetails.error.indexOf('ENOENT:') === 0 || refDetails.error.indexOf('Not Found') === 0) {
                            refDetails.error = 'JSON Pointer points to missing location: ' + refDetails.uri;
                        }
                    }

                    return;
                }

                refDeps = results.deps[refDetails.refdId];

                if (refDetails.refdId.indexOf(root) !== 0) {
                    Object.keys(refDeps).forEach(function (prop) {
                        walkRefs(refDetails.refdId, refDetails.refdId + prop.substr(1), refPath.concat(pathFromPtr(prop)));
                    });
                }
            }

            // For performance reasons, we only process a document (or sub document) and each reference once ever.  This means
            // that if we want to provide the full picture as to what paths in the resolved document were created as a result
            // of a reference, we have to take our fully-qualified reference locations and expand them to be all local based
            // on the original document.
            Object.keys(results.refs).forEach(function (refPtr) {
                var refDetails = results.refs[refPtr];
                var fqURISegments;
                var uriSegments;

                // Make all fully-qualified reference URIs relative to the document root (if necessary).  This step is done here
                // for performance reasons instead of below when the official sanitization process runs.
                if (refDetails.type !== 'invalid') {
                    // Remove the trailing hash from document root references if they weren't in the original URI
                    if (refDetails.fqURI[refDetails.fqURI.length - 1] === '#' &&
                        refDetails.uri[refDetails.uri.length - 1] !== '#') {
                        refDetails.fqURI = refDetails.fqURI.substr(0, refDetails.fqURI.length - 1);
                    }

                    fqURISegments = refDetails.fqURI.split('/');
                    uriSegments = refDetails.uri.split('/');

                    // The fully-qualified URI is unencoded so to keep the original formatting of the URI (encoded vs. unencoded),
                    // we need to replace each URI segment in reverse order.
                    _.times(uriSegments.length - 1, function (time) {
                        var nSeg = uriSegments[uriSegments.length - time - 1];
                        var pSeg = uriSegments[uriSegments.length - time];
                        var fqSegIndex = fqURISegments.length - time - 1;

                        if (nSeg === '.' || nSeg === '..' || pSeg === '..') {
                            return;
                        }

                        fqURISegments[fqSegIndex] = nSeg;
                    });

                    refDetails.fqURI = fqURISegments.join('/');

                    // Make the fully-qualified URIs relative to the document root
                    if (refDetails.fqURI.indexOf(fullLocation) === 0) {
                        refDetails.fqURI = refDetails.fqURI.replace(fullLocation, '');
                    } else if (refDetails.fqURI.indexOf(relativeBase) === 0) {
                        refDetails.fqURI = refDetails.fqURI.replace(relativeBase, '');
                    }

                    if (refDetails.fqURI[0] === '/') {
                        refDetails.fqURI = '.' + refDetails.fqURI;
                    }
                }

                // We only want to process references found at or beneath the provided document and sub-document path
                if (refPtr.indexOf(refsRoot) !== 0) {
                    return;
                }

                walkRefs(refsRoot, refPtr, pathFromPtr(refPtr.substr(refsRoot.length)));
            });

            // Sanitize the reference details
            _.forOwn(allRefs, function (refDetails, refPtr) {
                // Delete the reference id used for dependency tracking and circular identification
                delete refDetails.refdId;

                // For locally-circular references, update the $ref to be fully qualified (Issue #175)
                if (refDetails.circular && refDetails.type === 'local') {
                    refDetails.value.$ref = refDetails.fqURI;

                    setValue(results.docs[fullLocation], pathFromPtr(refPtr), refDetails.value);
                }

                // To avoid the error message being URI encoded/decoded by mistake, replace the current JSON Pointer with the
                // value in the JSON Reference definition.
                if (refDetails.missing) {
                    refDetails.error = refDetails.error.split(': ')[0] + ': ' + refDetails.def.$ref;
                }
            });

            return {
                refs: allRefs,
                resolved: results.docs[fullLocation]
            };
        });

    return allTasks;
}