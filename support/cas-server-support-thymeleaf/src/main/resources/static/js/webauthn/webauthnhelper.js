let ceremonyState = {};
let session = {};

function extend(obj, more) {
    return Object.assign({}, obj, more);
}

function rejectIfNotSuccess(response) {
    if (response.success) {
        return response;
    } else {
        return new Promise((resolve, reject) => reject(response));
    }
}

function updateSession(response) {
    if (response.sessionToken) {
        session.sessionToken = response.sessionToken;
    }
    if (response.username) {
        session.username = response.username;
    }
    updateSessionBox();
    return response;
}

function logout() {
    session = {};
    updateSession({});
}

function updateSessionBox() {
    if (session.username) {
        document.getElementById('session').textContent = `User: ${session.username}`;
        // document.getElementById('logoutButton').disabled = false;
    } else {
        document.getElementById('session').textContent = 'Unknown';
        // document.getElementById('logoutButton').disabled = true;
    }
}

function rejected(err) {
    return new Promise((resolve, reject) => reject(err));
}

function setStatus(statusText) {
    document.getElementById('status').textContent = statusText;
}

function addMessage(message) {
    const el = document.getElementById('messages');
    const p = document.createElement('p');
    p.appendChild(document.createTextNode(message));
    el.appendChild(p);
}

function addMessages(messages) {
    messages.forEach(addMessage);
}

function clearMessages() {
    const el = document.getElementById('messages');
    while (el.firstChild) {
        el.removeChild(el.firstChild);
    }
}

function showJson(name, data) {
    if (data != null) {
        document.getElementById(name).textContent = JSON.stringify(data, false, 4);
    }
}
function showRequest(data) { return showJson('request', data); }
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
    document.getElementById("device-info").style = "display: none";
}
function showDeviceInfo(params) {
    document.getElementById("device-info").style = undefined;
    document.getElementById("device-name").textContent = params.displayName;
    document.getElementById("device-nickname").textContent = params.nickname;
    document.getElementById("device-icon").src = params.imageUrl;
}

function resetDisplays() {
    clearMessages();
    showRequest(null);
    showAuthenticatorResponse(null);
    showServerResponse(null);
    hideDeviceInfo();
}

function getIndexActions() {
    return fetch('api/v1/')
        .then(response => response.json())
        .then(data => data.actions)
        ;
}

function getRegisterRequest(urls, username, displayName, credentialNickname, requireResidentKey = false) {
    return fetch(urls.register, {
        body: new URLSearchParams({
            username,
            displayName,
            credentialNickname,
            requireResidentKey,
            sessionToken: session.sessionToken || null,
        }),
        method: 'POST',
    })
        .then(response => response.json())
        .then(updateSession)
        .then(rejectIfNotSuccess)
        ;
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
        body: JSON.stringify(body),
    })
        .then(response => response.json())
        .then(updateSession);
}

function performCeremony(params) {
    const callbacks = params.callbacks || {}; 
    const getIndexActions = params.getIndexActions;
    const getRequest = params.getRequest;
    const statusStrings = params.statusStrings;
    const executeRequest = params.executeRequest;
    const handleError = params.handleError; /* function(err): ? */

    setStatus('Preparing...');
    resetDisplays();

    return getIndexActions()
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
                callbacks.authenticatorRequest({ request, urls });
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
        callbacks.serverRequest({ urls, request, response });
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

function register(requireResidentKey = false, getRequest = getRegisterRequest) {
    const username = document.getElementById('username').value;
    const displayName = "Misagh";
    const credentialNickname = "MisaghMoayyed";

    var request;

    return performCeremony({
        getIndexActions,
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
                const nicknameInfo = {
                    nickname: data.registration.credentialNickname,
                };

                if (data.registration && data.registration.attestationMetadata) {
                    showDeviceInfo(extend(
                        data.registration.attestationMetadata.deviceProperties,
                        nicknameInfo
                    ));
                } else {
                    showDeviceInfo(nicknameInfo);
                }

                if (!data.attestationTrusted) {
                    addMessage("Warning: Attestation cannot be trusted.");
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
                addMessage(`${err.name}: ${err.message}`);
            } else if (err.messages) {
                addMessages(err.messages);
            }
            return rejected(err);
        });
}

function getAuthenticateRequest(urls, username) {
    return fetch(urls.authenticate, {
        body: new URLSearchParams(username ? { username } : {}),
        method: 'POST',
    })
        .then(response => response.json())
        .then(updateSession)
        .then(rejectIfNotSuccess)
        ;
}

function executeAuthenticateRequest(request) {
    console.log('Sending authentication request', request);
    return webauthn.getAssertion(request.publicKeyCredentialRequestOptions);
}

function authenticate(username = null, getRequest = getAuthenticateRequest) {
    return performCeremony({
        getIndexActions,
        getRequest: urls => getRequest(urls, username),
        statusStrings: {
            init: 'Initiating authentication ceremony...',
            authenticatorRequest: 'Asking authenticators to perform assertion...',
            success: 'Authentication successful.',
        },
        executeRequest: executeAuthenticateRequest,
    }).then(data => {
        if (data.registrations) {
            addMessage(`Authenticated as: ${data.registrations[0].username}`);
        }
        return data;
    }).catch((err) => {
        setStatus('Authentication failed.');
        if (err.name === 'InvalidStateError') {
            addMessage(`This authenticator is not registered for the account "${username}".`)
        } else if (err.message) {
            addMessage(`${err.name}: ${err.message}`);
        } else if (err.messages) {
            addMessages(err.messages);
        }
        console.error('Authentication failed', err);
        return rejected(err);
    });
}

function deregister() {
    const credentialId = document.getElementById('deregisterCredentialId').value;
    addMessage('Deregistering credential...');

    return getIndexActions()
        .then(urls =>
            fetch(urls.deregister, {
                body: new URLSearchParams({
                    credentialId,
                    sessionToken: session.sessionToken || null,
                }),
                method: 'POST',
            })
        )
        .then(response => response.json())
        .then(updateSession)
        .then(rejectIfNotSuccess)
        .then(data => {
            if (data.success) {
                if (data.droppedRegistration) {
                    addMessage(`Successfully deregistered credential: ${data.droppedRegistration.credentialNickname || credentialId}`);
                } else {
                    addMessage(`Successfully deregistered credential: ${credentialId}`);
                }
                if (data.accountDeleted) {
                    addMessage('No credentials remain - account deleted.');
                    logout();
                }
            } else {
                addMessage('Credential deregistration failed.');
            }
        })
        .catch((err) => {
            setStatus('Credential deregistration failed.');
            if (err.message) {
                addMessage(`${err.name}: ${err.message}`);
            } else if (err.messages) {
                addMessages(err.messages);
            }
            console.error('Authentication failed', err);
            return rejected(err);
        });
}

function init() {
    hideDeviceInfo();
    return false;
}

window.onload = init;
