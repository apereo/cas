/**********************************
 * Base64 Core
 **********************************/
(function (root, factory) {
    if (typeof define === 'function' && define.amd) {
        define(['base64js'], factory);
    } else if (typeof module === 'object' && module.exports) {
        module.exports = factory(require('base64js'));
    } else {
        root.base64url = factory(root.base64js);
    }
})(this, function (base64js) {

    function ensureUint8Array(arg) {
        if (arg instanceof ArrayBuffer) {
            return new Uint8Array(arg);
        } else {
            return arg;
        }
    }

    function base64UrlToMime(code) {
        return code.replace(/-/g, '+').replace(/_/g, '/') + '===='.substring(0, (4 - (code.length % 4)) % 4);
    }

    function mimeBase64ToUrl(code) {
        return code.replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
    }

    function fromByteArray(bytes) {
        return mimeBase64ToUrl(base64js.fromByteArray(ensureUint8Array(bytes)));
    }

    function toByteArray(code) {
        return base64js.toByteArray(base64UrlToMime(code));
    }

    return {
        fromByteArray: fromByteArray,
        toByteArray: toByteArray,
    };

});


/*******************************************************
 * WebAuthN Core
 *******************************************************/

(function (root, factory) {
    if (typeof define === 'function' && define.amd) {
        define(['base64url'], factory);
    } else if (typeof module === 'object' && module.exports) {
        module.exports = factory(require('base64url'));
    } else {
        root.webauthn = factory(root.base64url);
    }
})(this, function (base64url) {

    function extend(obj, more) {
        return Object.assign({}, obj, more);
    }

    /**
     * Create a WebAuthn credential.
     *
     * @param request: object - A PublicKeyCredentialCreationOptions object, except
     *   where binary values are base64url encoded strings instead of byte arrays
     *
     * @return a PublicKeyCredentialCreationOptions suitable for passing as the
     *   `publicKey` parameter to `navigator.credentials.create()`
     */
    function decodePublicKeyCredentialCreationOptions(request) {
        const excludeCredentials = request.excludeCredentials.map(credential => extend(
            credential, {
                id: base64url.toByteArray(credential.id),
            }));

        const publicKeyCredentialCreationOptions = extend(
            request, {
                attestation: 'direct',
                user: extend(
                    request.user, {
                        id: base64url.toByteArray(request.user.id),
                    }),
                challenge: base64url.toByteArray(request.challenge),
                excludeCredentials,
            });

        return publicKeyCredentialCreationOptions;
    }

    /**
     * Create a WebAuthn credential.
     *
     * @param request: object - A PublicKeyCredentialCreationOptions object, except
     *   where binary values are base64url encoded strings instead of byte arrays
     *
     * @return the Promise returned by `navigator.credentials.create`
     */
    function createCredential(request) {
        return navigator.credentials.create({
            publicKey: decodePublicKeyCredentialCreationOptions(request),
        });
    }

    /**
     * Perform a WebAuthn assertion.
     *
     * @param request: object - A PublicKeyCredentialRequestOptions object,
     *   except where binary values are base64url encoded strings instead of byte
     *   arrays
     *
     * @return a PublicKeyCredentialRequestOptions suitable for passing as the
     *   `publicKey` parameter to `navigator.credentials.get()`
     */
    function decodePublicKeyCredentialRequestOptions(request) {
        const allowCredentials = request.allowCredentials && request.allowCredentials.map(credential => extend(
            credential, {
                id: base64url.toByteArray(credential.id),
            }));

        const publicKeyCredentialRequestOptions = extend(
            request, {
                allowCredentials,
                challenge: base64url.toByteArray(request.challenge),
            });

        return publicKeyCredentialRequestOptions;
    }

    /**
     * Perform a WebAuthn assertion.
     *
     * @param request: object - A PublicKeyCredentialRequestOptions object,
     *   except where binary values are base64url encoded strings instead of byte
     *   arrays
     *
     * @return the Promise returned by `navigator.credentials.get`
     */
    function getAssertion(request) {
        console.log('Get assertion', request);
        return navigator.credentials.get({
            publicKey: decodePublicKeyCredentialRequestOptions(request),
        });
    }


    /** Turn a PublicKeyCredential object into a plain object with base64url encoded binary values */
    function responseToObject(response) {
        if (response.u2fResponse) {
            return response;
        } else {
            let clientExtensionResults = {};

            try {
                clientExtensionResults = response.getClientExtensionResults();
            } catch (e) {
                console.error('getClientExtensionResults failed', e);
            }

            if (response.response.attestationObject) {
                return {
                    type: response.type,
                    id: response.id,
                    response: {
                        attestationObject: base64url.fromByteArray(response.response.attestationObject),
                        clientDataJSON: base64url.fromByteArray(response.response.clientDataJSON),
                    },
                    clientExtensionResults,
                };
            } else {
                return {
                    type: response.type,
                    id: response.id,
                    response: {
                        authenticatorData: base64url.fromByteArray(response.response.authenticatorData),
                        clientDataJSON: base64url.fromByteArray(response.response.clientDataJSON),
                        signature: base64url.fromByteArray(response.response.signature),
                        userHandle: response.response.userHandle && base64url.fromByteArray(response.response.userHandle),
                    },
                    clientExtensionResults,
                };
            }
        }
    }

    return {
        decodePublicKeyCredentialCreationOptions,
        decodePublicKeyCredentialRequestOptions,
        createCredential,
        getAssertion,
        responseToObject,
    };

});


/*****************************************************
 * WebAuthn Utilities
 *****************************************************/

let ceremonyState = {};
let session = {};

function extend(obj, more) {
    return Object.assign({}, obj, more);
}

function rejectIfNotSuccess(response) {
    if (response.success) {
        return response;
    }
    return new Promise((resolve, reject) => reject(response));
}

function updateSession(response) {
    if (response.sessionToken) {
        session.sessionToken = response.sessionToken;
    } else {
        session.sessionToken = null;
    }
    if (response.username) {
        session.username = response.username;
    } else {
        session.username = null;
    }
    updateSessionBox();
    return response;
}

function logout() {
    session = {};
    updateSession({});
}

function updateSessionBox() {
    const disabled = session.username == null || session.username === '';
    $('#logoutButton').prop('disabled', disabled);
}

function rejected(err) {
    return new Promise((resolve, reject) => reject(err));
}

function setStatus(statusText) {
    $('#status').val(statusText);
}

function addDeviceAttributeAsRow(name, value) {
    let row = `<tr class="mdc-data-table__row">`
        + `<td class="mdc-data-table__cell"><code>${name}</code></td>`
        + `<td class="mdc-data-table__cell"><code>${value}</code></td>`
        + `</tr>`;
    $('#deviceTable tbody').append(row);
}

function addMessage(message) {
    $('#messages').html("<p>" + message + "</p>");
}

function addMessages(messages) {
    messages.forEach(addMessage);
}

function showJson(name, data) {
    if (data != null) {
        $('#' + name).text(JSON.stringify(data, false, 4));
    }
}

function showRequest(data) {
    return showJson('request', data);
}

function showAuthenticatorResponse(data) {
    const clientDataJson = data && (data.response && data.response.clientDataJSON);
    return showJson('authenticator-response', extend(
        data, {
            _clientDataJson: data && JSON.parse(new TextDecoder('utf-8').decode(base64url.toByteArray(clientDataJson))),
        }));
}

function showServerResponse(data) {
    if (data && data.messages) {
        addMessages(data.messages);
    }
    return showJson('server-response', data);
}

function hideDeviceInfo() {
    $("#device-info").hide();
    $("#registerButton").show();
    $("#registerDiscoverableCredentialButton").show();
}

function showDeviceInfo(params) {
    $("#device-info").show();
    $("#device-name").text(params.displayName);
    $("#device-icon").attr("src", params.imageUrl);
    $("#registerButton").hide();
    $("#deviceNamePanel").hide();

    $("#registerDiscoverableCredentialButton").hide();
    $("#residentKeysPanel").hide();
}

function resetDisplays() {
    /*
    showRequest(null);
    showAuthenticatorResponse(null);
    showServerResponse(null);
     */
    hideDeviceInfo();
}

function getWebAuthnUrls() {
    let endpoints = {
        authenticate: "webauthn/authenticate",
        register: "webauthn/register",
    }
    return new Promise((resolve, reject) => resolve(endpoints)).then(data => {
        return data;
    });
}

function getRegisterRequest(urls,
                            username,
                            displayName,
                            credentialNickname,
                            requireResidentKey = false) {
    return fetch(urls.register, {
        body: new URLSearchParams({
            username,
            displayName,
            credentialNickname,
            requireResidentKey,
            sessionToken: session.sessionToken || null,
        }),
        headers: {
           "X-CSRF-TOKEN": csrfToken
        },
        method: 'POST',
    })
        .then(response => response.json())
        .then(updateSession)
        .then(rejectIfNotSuccess);
}

function executeRegisterRequest(request) {
    return webauthn.createCredential(request.publicKeyCredentialCreationOptions);
}

function submitResponse(url, request, response) {
    const body = {
        requestId: request.requestId,
        credential: response,
        sessionToken: request.sessionToken || session.sessionToken || null,
    };

    return fetch(url, {
        method: 'POST',
        headers: {
            "X-CSRF-TOKEN": csrfToken
        },
        body: JSON.stringify(body),
    })
        .then(response => response.json())
        .then(updateSession);
}

function performCeremony(params) {
    const callbacks = params.callbacks || {};
    const getWebAuthnUrls = params.getWebAuthnUrls;
    const getRequest = params.getRequest;
    const statusStrings = params.statusStrings;
    const executeRequest = params.executeRequest;

    resetDisplays();

    return getWebAuthnUrls()
        .then(urls => {
            setStatus(statusStrings.int);
            if (callbacks.init) {
                callbacks.init(urls);
            }
            return getRequest(urls);
        })

        .then((params) => {
            const request = params.request;
            const urls = params.actions;
            setStatus(statusStrings.authenticatorRequest);
            if (callbacks.authenticatorRequest) {
                callbacks.authenticatorRequest({request, urls});
            }
            showRequest(request);
            ceremonyState = {
                callbacks,
                request,
                statusStrings,
                urls
            };
            return executeRequest(request)
                .then(webauthn.responseToObject);
        })
        .then(finishCeremony);
}

function finishCeremony(response) {
    const callbacks = ceremonyState.callbacks;
    const request = ceremonyState.request;
    const statusStrings = ceremonyState.statusStrings;
    const urls = ceremonyState.urls;

    setStatus(statusStrings.serverRequest || 'Sending response to server...');
    if (callbacks.serverRequest) {
        callbacks.serverRequest({urls, request, response});
    }
    showAuthenticatorResponse(response);

    return submitResponse(urls.finish, request, response)
        .then(data => {
            if (data && data.success) {
                setStatus(statusStrings.success);
            } else {
                setStatus('Error');
            }
            showServerResponse(data);
            return data;
        });
}

function register(username, displayName, credentialNickname, csrfToken,
                  requireResidentKey = false,
                  getRequest = getRegisterRequest) {
    let request;
    return performCeremony({
        getWebAuthnUrls,
        getRequest: urls => getRequest(urls, username, displayName, credentialNickname, requireResidentKey),
        statusStrings: {
            init: 'Initiating registration ceremony with server...',
            authenticatorRequest: 'Asking authenticators to create credential...',
            success: 'Registration successful.',
        },
        executeRequest: req => {
            request = req;
            return executeRegisterRequest(req);
        }
    })
        .then(data => {
            if (data.registration) {
                const nicknameInfo = {nickname: data.registration.credentialNickname};

                if (data.registration && data.registration.attestationMetadata) {
                    showDeviceInfo(extend(
                        data.registration.attestationMetadata.deviceProperties,
                        nicknameInfo
                    ));
                } else {
                    showDeviceInfo(nicknameInfo);
                }

                if (!data.attestationTrusted) {
                    addMessage("Attestation cannot be trusted.");
                } else {
                    setTimeout(function () {
                        $('#sessionToken').val(session.sessionToken);
                        console.log("Submitting registration form");
                        $('#form').submit();
                    }, 1500);
                }
            }
        })
        .catch((err) => {
            setStatus('Registration failed.');
            console.error('Registration failed', err);

            if (err.name === 'NotAllowedError') {
                if (request.publicKeyCredentialCreationOptions.excludeCredentials
                    && request.publicKeyCredentialCreationOptions.excludeCredentials.length > 0
                ) {
                    addMessage('Credential creation failed, probably because an already registered credential is available.');
                } else {
                    addMessage('Credential creation failed for an unknown reason.');
                }
            } else if (err.name === 'InvalidStateError') {
                addMessage(`This authenticator is already registered for the account "${username}".`)
            } else if (err.message) {
                addMessage(`${err.message}`);
            } else if (err.messages) {
                addMessages(err.messages);
            }
            return rejected(err);
        });
}

function getAuthenticateRequest(urls, username) {
    return fetch(urls.authenticate, {
        body: new URLSearchParams(username ? {username} : {}),
        headers: {
            "X-CSRF-TOKEN": csrfToken
        },
        method: 'POST',
    })
        .then(response => response.json())
        .then(updateSession)
        .then(rejectIfNotSuccess);
}

function executeAuthenticateRequest(request) {
    console.log('Sending authentication request', request);
    return webauthn.getAssertion(request.publicKeyCredentialRequestOptions);
}

function authenticate(username = null, getRequest = getAuthenticateRequest) {
    $('#deviceTable tbody tr').remove();
    $('#divDeviceInfo').hide();
    hideDeviceInfo();

    return performCeremony({
        getWebAuthnUrls,
        getRequest: urls => getRequest(urls, username),
        statusStrings: {
            init: 'Initiating authentication ceremony...',
            authenticatorRequest: 'Asking authenticators to perform assertion...',
            success: 'Authentication successful.',
        },
        executeRequest: executeAuthenticateRequest,
    }).then(data => {
        $('#divDeviceInfo').show();
        console.log("Received: " + JSON.stringify(data, undefined, 2));
        if (data.registrations) {

            data.registrations.forEach(reg => {

                addDeviceAttributeAsRow("Username", reg.username);
                addDeviceAttributeAsRow("Credential Nickname", reg.credentialNickname);
                addDeviceAttributeAsRow("Registration Date", reg.registrationTime);
                addDeviceAttributeAsRow("Session Token", data.sessionToken);
                addDeviceAttributeAsRow("Device Id", reg.attestationMetadata.deviceProperties.deviceId);
                addDeviceAttributeAsRow("Device Name", reg.attestationMetadata.deviceProperties.displayName);

                showDeviceInfo({
                    "displayName": reg.attestationMetadata.deviceProperties.displayName,
                    "imageUrl": reg.attestationMetadata.deviceProperties.imageUrl
                })
            });

            $('#authnButton').hide();

            setTimeout(function () {
                $('#token').val(data.sessionToken);
                console.log("Submitting authentication form");
                $('#webauthnLoginForm').submit();
            }, 1500);
        }
        return data;
    }).catch((err) => {
        setStatus(authFailTitle);
        if (err.name === 'InvalidStateError') {
            addMessage(`This authenticator is not registered for the account "${username}".`)
        } else if (err.message) {
            addMessage(`${err.name}: ${err.message}`);
        } else if (err.messages) {
            addMessages(err.messages);
        }
        console.error('Authentication failed', err);
        addMessage(authFailDesc);
        return rejected(err);
    });
}

function init() {
    hideDeviceInfo();
    return false;
}

window.onload = init;
