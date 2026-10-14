/**********************************
 * Base64 Core
 **********************************/
((root, factory) => {
    if (typeof define === "function" && define.amd) {
        define(['base64js'], factory);
    } else if (typeof module === "object" && module.exports) {
        module.exports = factory(require("base64js"));
    } else {
        root.base64url = factory(root.base64js);
    }
})(this, base64js => {

    function ensureUint8Array(arg) {
        if (arg instanceof ArrayBuffer) {
            return new Uint8Array(arg);
        } else {
            return arg;
        }
    }

    function base64UrlToMime(code) {
        return code.replace(/-/g, "+").replace(/_/g, '/') + '===='.substring(0, (4 - (code.length % 4)) % 4);
    }

    function mimeBase64ToUrl(code) {
        return code.replace(/\+/g, "-").replace(/\//g, '_').replace(/=/g, '');
    }

    function fromByteArray(bytes) {
        return mimeBase64ToUrl(base64js.fromByteArray(ensureUint8Array(bytes)));
    }

    function toByteArray(code) {
        return base64js.toByteArray(base64UrlToMime(code));
    }

    return {
        fromByteArray,
        toByteArray,
    };

});


/*******************************************************
 * WebAuthN Core
 *******************************************************/

((root, factory) => {
    if (typeof define === 'function' && define.amd) {
        define([], factory);
    } else if (typeof module === 'object' && module.exports) {
        module.exports = factory();
    } else {
        root.webauthn = factory();
    }
})(this, () => {

    /**
     * Turn the JSON form of PublicKeyCredentialCreationOptions sent by CAS, where binary values are
     * base64url encoded strings, into the options expected by `navigator.credentials.create()`.
     */
    function decodePublicKeyCredentialCreationOptions(request) {
        return PublicKeyCredential.parseCreationOptionsFromJSON(request);
    }

    /**
     * Create a WebAuthn credential from the JSON form of PublicKeyCredentialCreationOptions.
     *
     * @return the Promise returned by `navigator.credentials.create`
     */
    function createCredential(request) {
        return navigator.credentials.create({
            publicKey: decodePublicKeyCredentialCreationOptions(request),
        });
    }

    /**
     * Turn the JSON form of PublicKeyCredentialRequestOptions sent by CAS, where binary values are
     * base64url encoded strings, into the options expected by `navigator.credentials.get()`.
     */
    function decodePublicKeyCredentialRequestOptions(request) {
        return PublicKeyCredential.parseRequestOptionsFromJSON(request);
    }

    /**
     * Perform a WebAuthn assertion from the JSON form of PublicKeyCredentialRequestOptions.
     *
     * @return the Promise returned by `navigator.credentials.get`
     */
    function getAssertion(request) {
        return navigator.credentials.get({
            publicKey: decodePublicKeyCredentialRequestOptions(request),
        });
    }

    /**
     * Turn a PublicKeyCredential into its JSON form, with base64url encoded binary values, transports,
     * authenticator attachment and client extension results.
     */
    function responseToObject(response) {
        return response.toJSON();
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

/**
 * Checks if the browser supports WebAuthn as these pages use it, including the WebAuthn Level 3 JSON
 * serialization of credential options. This is a synchronous check so that callers can test its result
 * directly; whether a platform authenticator exists is not checked, since security keys and phones work too.
 */
function isBrowserSupported() {
    const supported = window.PublicKeyCredential !== undefined
        && typeof PublicKeyCredential.parseCreationOptionsFromJSON === "function"
        && typeof PublicKeyCredential.parseRequestOptionsFromJSON === "function";
    if (!supported) {
        console.error("WebAuthn is not supported in this browser.");
    }
    return supported;
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
    $("#logoutButton").prop('disabled', disabled);
}

function rejected(err) {
    return new Promise((resolve, reject) => reject(err));
}

function setStatus(statusText) {
    $("#status").text(statusText);
}

function addDeviceAttributeAsRow(name, value) {
    let row = "<tr class=\"mdc-data-table__row\">"
        + `<td class="mdc-data-table__cell"><code>${name}</code></td>`
        + `<td class="mdc-data-table__cell"><code>${value}</code></td>`
        + "</tr>";
    $("#deviceTable tbody").append(row);
}

function addMessage(message) {
    $('#messages').html(`<p>${message}</p>`);
}

function clearMessages() {
    $('#messages').empty();
}

function addMessages(messages) {
    messages.forEach(addMessage);
}

function showJson(name, data) {
    if (data != null) {
        $(`#${name}`).text(JSON.stringify(data, false, 4));
    }
}

function showRequest(data) {
    return showJson("request", data);
}

function showAuthenticatorResponse(data) {
    const clientDataJson = data && (data.response && data.response.clientDataJSON);
    return showJson("authenticator-response", extend(
        data, {
            _clientDataJson: data && JSON.parse(new TextDecoder("utf-8").decode(base64url.toByteArray(clientDataJson))),
        }));
}

function showServerResponse(data) {
    if (data && data.messages) {
        addMessages(data.messages);
    }
    return showJson("server-response", data);
}

function hideDeviceInfo() {
    $("#device-info").hide();
    $("#registrationChoices").show();
    $("#registerButton").show();
    $("#registerDiscoverableCredentialButton").show();
}

/**
 * Show the registered or authenticated device. The icon comes from attestation metadata and is shown only when
 * that metadata provides one and it loads; otherwise the browser would draw a broken-image placeholder. Without a
 * device name from the metadata, the credential nickname is shown instead.
 */
function showDeviceInfo(params) {
    $("#device-info").show();
    $("#device-name").text(params.displayName || params.nickname || "");
    const icon = $("#device-icon");
    if (params.imageUrl) {
        icon.off("error").one("error", () => icon.hide()).attr("src", params.imageUrl).show();
    } else {
        icon.removeAttr("src").hide();
    }
    $("#registerButton").hide();
    $("#deviceNamePanel").hide();
    $("#registrationChoices").hide();

    $("#registerDiscoverableCredentialButton").hide();
    $("#residentKeysPanel").hide();
}

/**
 * Show the device that was just registered: the device named by attestation metadata when there is one, otherwise
 * the passkey provider that CAS recognizes from the authenticator's AAGUID, and the credential nickname as a last resort.
 */
function showRegisteredDevice(data) {
    const metadata = data.registration.attestationMetadata;
    const provider = data.passkeyProvider || {};
    showDeviceInfo(extend(
        extend({displayName: provider.name, imageUrl: provider.icon}, metadata ? metadata.deviceProperties : {}),
        {nickname: data.registration.credentialNickname}
    ));
}

/**
 * Show the device whose credential answered the authentication: the device named by attestation metadata when there
 * is one, otherwise the passkey provider that CAS recognizes from the authenticator's AAGUID, and the credential
 * nickname as a last resort.
 */
function showAuthenticatedDevice(data) {
    const credentialId = data.response && data.response.credential ? data.response.credential.id : undefined;
    const registration = (data.registrations || []).find(reg => reg.credential && reg.credential.credentialId === credentialId);
    if (registration) {
        showRegisteredDevice({registration, passkeyProvider: data.passkeyProvider});
    }
}

function resetDisplays() {
    /*
    showRequest(null);
    showAuthenticatorResponse(null);
    showServerResponse(null);
     */
    hideDeviceInfo();
    clearMessages()
}

function getWebAuthnUrls() {
    
    const endpoints = {
        authenticate: `${window.location.origin}${contextPath}/webauthn/authenticate`,
        register: `${window.location.origin}${contextPath}/webauthn/register`,
    };
    // console.log(endpoints);
    return new Promise((resolve, reject) => resolve(endpoints)).then(data => data);
}

function getRegisterRequest(urls,
                            username,
                            displayName,
                            credentialNickname,
                            requireResidentKey = false) {
    let execution = document.getElementById('execution').value;
    const headers = {};
    if (csrfToken !== undefined) {
        headers["X-CSRF-TOKEN"] = csrfToken;
    }
    return fetch(urls.register, {
        body: new URLSearchParams({
            username,
            displayName,
            credentialNickname,
            requireResidentKey,
            execution: execution,
            sessionToken: session.sessionToken || null,
        }),
        headers: headers,
        method: "POST",
    })
        .then((response) => response.json())
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

    const headers = {};
    if (csrfToken !== undefined) {
        headers["X-CSRF-TOKEN"] = csrfToken;
    }
    return fetch(url, {
        method: 'POST',
        headers: headers,
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
        .then((urls) => {
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

    setStatus(statusStrings.serverRequest || "Sending response to server...");
    if (callbacks.serverRequest) {
        callbacks.serverRequest({urls, request, response});
    }
    showAuthenticatorResponse(response);

    const finishUrl = `${window.location.origin}${contextPath}${urls.finish}`;
    return submitResponse(finishUrl, request, response)
        .then((data) => {
            if (request.publicKeyCredentialRequestOptions) {
                signalPasskeyState(request, data, response);
            }
            if (data && data.success) {
                setStatus(statusStrings.success);
            } else {
                setStatus("Error");
            }
            showServerResponse(data);
            return data;
        });
}

function register(username, displayName, credentialNickname, csrfToken,
                  requireResidentKey = false,
                  getRequest = getRegisterRequest) {

    if (!isBrowserSupported()) {
        addMessage('Web Authentication (WebAuthn) is not supported by this browser.');
        return rejected('Unsupported browser');
    }

    let request;
    return performCeremony({
        getWebAuthnUrls,
        getRequest: urls => getRequest(urls, username, displayName, credentialNickname, requireResidentKey, csrfToken),
        statusStrings: {
            init: "Initiating registration ceremony with server...",
            authenticatorRequest: "Asking authenticators to create credential...",
            success: 'Registration successful.',
        },
        executeRequest: (req) => {
            request = req;
            return executeRegisterRequest(req);
        }
    })
        .then(data => {
            // console.log(`data: ${JSON.stringify(data)}`);
            clearMessages();
            if (data.registration) {
                showRegisteredDevice(data);

                if (!data.attestationTrusted) {
                    addMessage("Attestation cannot be trusted.");
                } else {
                    setTimeout(() => {
                        $("#sessionToken").val(session.sessionToken);
                        // console.log("Submitting registration form");
                        $("#form").submit();
                    }, 2500);
                }
            }
        })
        .catch((err) => {
            setStatus("Registration failed.");
            console.error("Registration failed", err);
            clearMessages();
            
            if (err.name === "NotAllowedError") {
                if (request.publicKeyCredentialCreationOptions.excludeCredentials
                    && request.publicKeyCredentialCreationOptions.excludeCredentials.length > 0
                ) {
                    addMessage("Credential creation failed, probably because an already registered credential is available.");
                } else {
                    addMessage("Credential creation failed for an unknown reason.");
                }
            } else if (err.name === "InvalidStateError") {
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
    const headers = {};
    if (csrfToken !== undefined && csrfToken !== null) {
        headers["X-CSRF-TOKEN"] = csrfToken;
    }
    return fetch(urls.authenticate, {
        body: new URLSearchParams(username ? {username} : {}),
        headers: headers,
        method: 'POST',
    })
        .then(response => response.json())
        .then(updateSession)
        .then(rejectIfNotSuccess);
}

function executeAuthenticateRequest(request) {
    // console.log('Sending authentication request', request);
    return webauthn.getAssertion(request.publicKeyCredentialRequestOptions);
}

function authenticate(username = null, getRequest = getAuthenticateRequest) {
    $("#deviceTable tbody tr").remove();
    $("#divDeviceInfo").hide();
    hideDeviceInfo();
    clearMessages();

    if (!isBrowserSupported()) {
        setStatus(authFailTitle);
        addMessage('Web Authentication (WebAuthn) is not supported by this browser.');
        return rejected('Unsupported browser');
    }

    // console.log(`Starting authentication for username ${username}`);
    return performCeremony({
        getWebAuthnUrls,
        getRequest: (urls) => getRequest(urls, username),
        statusStrings: {
            init: "Initiating authentication ceremony...",
            authenticatorRequest: "Asking authenticators to perform assertion...",
            success: "Authentication successful.",
        },
        executeRequest: executeAuthenticateRequest,
    }).then((data) => {
        clearMessages();
        // console.log(`Received: ${JSON.stringify(data, undefined, 2)}`);
        if (data.registrations) {
            $('#divDeviceInfo').show();
            data.registrations.forEach((reg) => {

                addDeviceAttributeAsRow("Username", reg.username);
                addDeviceAttributeAsRow("Credential Nickname", reg.credentialNickname);
                addDeviceAttributeAsRow("Registration Date", reg.registrationTime);
                addDeviceAttributeAsRow("Session Token", data.sessionToken);
                if (reg.attestationMetadata) {
                    const deviceProperties = reg.attestationMetadata.deviceProperties;
                    if (deviceProperties) {
                        addDeviceAttributeAsRow("Device Id", deviceProperties.deviceId);
                        addDeviceAttributeAsRow("Device Name", deviceProperties.displayName);
                    }
                }
            });
            showAuthenticatedDevice(data);

            $("#authnButton").hide();

            Swal.fire({
                icon: "info",
                title: `Finalizing attempt for ${username}`,
                text: "Please wait while your authentication attempt is processed...",
                allowOutsideClick: false,
                showConfirmButton: false,
                didOpen: () => Swal.showLoading()
            });
            
            setTimeout(() => {
                $("#token").val(data.sessionToken);
                const form = QRCodeAuthentication ? $("#webauthnQRCodeVerifyForm") : $('#webauthnLoginForm');
                clearMessages();
                hideDeviceInfo();
                Swal.close();
                form.submit();
            }, 2000);
        }
        return data;
    }).catch((err) => {
        setStatus(authFailTitle);
        clearMessages();
        if (err.name === "InvalidStateError") {
            addMessage(`This authenticator is not registered for the account "${username}".`)
        } else if (err.message) {
            addMessage(`${err.name}: ${err.message}`);
        } else if (err.messages) {
            addMessages(err.messages);
        }
        console.error("Authentication failed", err);
        addMessage(authFailDesc);
        return rejected(err);
    });
}

async function isConditionalMediationAvailable() {
    try {
        if (window.PublicKeyCredential === undefined) {
            return false;
        }
        if (typeof window.PublicKeyCredential.getClientCapabilities === "function") {
            const capabilities = await window.PublicKeyCredential.getClientCapabilities();
            if (capabilities.conditionalGet !== undefined) {
                return capabilities.conditionalGet === true;
            }
        }
        return typeof window.PublicKeyCredential.isConditionalMediationAvailable === "function"
            && await window.PublicKeyCredential.isConditionalMediationAvailable();
    } catch (err) {
        console.error("Unable to determine support for conditional mediation", err);
        return false;
    }
}

/**
 * Tell the browser, through the WebAuthn Signal API, what CAS knows about the passkey that answered.
 * When CAS reports the credential as unknown, the browser is asked to stop offering it. After a successful
 * authentication, the browser learns which passkeys CAS still accepts for the user and what the user's current
 * name is, so that passkeys removed from CAS stop being offered and renamed accounts show up to date. The name is the
 * one the passkey was created with when that differs from the principal id, such as the username typed at login for a
 * passkey upgrade, so that the password manager keeps showing the name it knows.
 * Browsers without the Signal API ignore this.
 */
function signalPasskeyState(request, data, credential) {
    if (window.PublicKeyCredential === undefined || !data) {
        return;
    }
    const rpId = request.publicKeyCredentialRequestOptions.rpId;
    if (!rpId) {
        return;
    }
    const report = err => console.debug("WebAuthn signal was not delivered", err);
    try {
        if (data.unknownCredential) {
            if (credential && typeof PublicKeyCredential.signalUnknownCredential === "function") {
                PublicKeyCredential.signalUnknownCredential({
                    rpId,
                    credentialId: credential.id
                }).catch(report);
            }
            return;
        }
        const registrations = data.success ? data.registrations : undefined;
        const user = registrations && registrations.length > 0 ? registrations[0].userIdentity : undefined;
        if (!user) {
            return;
        }
        if (typeof PublicKeyCredential.signalAllAcceptedCredentials === "function") {
            PublicKeyCredential.signalAllAcceptedCredentials({
                rpId,
                userId: user.id,
                allAcceptedCredentialIds: registrations.map(reg => reg.credential.credentialId)
            }).catch(report);
        }
        if (typeof PublicKeyCredential.signalCurrentUserDetails === "function") {
            const used = credential ? registrations.find(reg => reg.credential && reg.credential.credentialId === credential.id) : undefined;
            PublicKeyCredential.signalCurrentUserDetails({
                rpId,
                userId: user.id,
                name: (used && used.userEntityName) || user.name,
                displayName: user.displayName
            }).catch(report);
        }
    } catch (err) {
        report(err);
    }
}

/**
 * Authenticate with a discoverable passkey and submit the resulting session token with the given form.
 * The request is made before anyone is authenticated, so it carries no allowed credentials and the
 * passkey that answers decides who logs in. With mediation set to "conditional", the browser offers
 * passkeys from the autofill menu of an input whose autocomplete attribute ends with "webauthn",
 * and the returned promise stays pending until the user picks one.
 */
async function authenticateWithPasskey(form, mediation = undefined) {
    const urls = await getWebAuthnUrls();
    const params = await getAuthenticateRequest(urls, null);
    const request = params.request;
    const options = {
        publicKey: webauthn.decodePublicKeyCredentialRequestOptions(request.publicKeyCredentialRequestOptions)
    };
    if (mediation !== undefined) {
        options.mediation = mediation;
    }
    const credential = webauthn.responseToObject(await navigator.credentials.get(options));
    const finishUrl = `${window.location.origin}${contextPath}${params.actions.finish}`;
    const data = await submitResponse(finishUrl, request, credential);
    signalPasskeyState(request, data, credential);
    if (data && data.success && data.sessionToken) {
        $(form).find("input[name=token]").val(data.sessionToken);
        $(form).submit();
    }
    return data;
}

/**
 * Offer a passkey to the user who just signed in with a password (WebAuthn conditional create). Where the browser
 * reports the conditionalCreate capability, CAS starts a conditional registration named after the username typed at
 * login, and the password manager that filled that password may create a passkey without a prompt; it refuses
 * silently otherwise. Whatever happens, including errors and a ceremony that does not finish within the timeout,
 * the given form is submitted so that the login continues.
 */
async function upgradeToPasskey(form, username, displayName, timeout = 5000) {
    try {
        if (isBrowserSupported() && typeof PublicKeyCredential.getClientCapabilities === "function") {
            const capabilities = await PublicKeyCredential.getClientCapabilities();
            if (capabilities.conditionalCreate === true) {
                const urls = await getWebAuthnUrls();
                const headers = {};
                if (csrfToken !== undefined && csrfToken !== null) {
                    headers["X-CSRF-TOKEN"] = csrfToken;
                }
                const response = await fetch(urls.register, {
                    method: "POST",
                    headers: headers,
                    body: new URLSearchParams({
                        username,
                        displayName,
                        conditional: true,
                        requireResidentKey: true
                    })
                });
                const data = await response.json();
                if (data.success) {
                    const controller = new AbortController();
                    const timer = setTimeout(() => controller.abort(), timeout);
                    try {
                        const credential = await navigator.credentials.create({
                            publicKey: webauthn.decodePublicKeyCredentialCreationOptions(data.request.publicKeyCredentialCreationOptions),
                            mediation: "conditional",
                            signal: controller.signal
                        });
                        const finishUrl = `${window.location.origin}${contextPath}${data.actions.finish}`;
                        await submitResponse(finishUrl, data.request, webauthn.responseToObject(credential));
                    } finally {
                        clearTimeout(timer);
                    }
                }
            }
        }
    } catch (err) {
        console.debug("Passkey upgrade did not complete", err);
    }
    $(form).submit();
}

function init() {
    hideDeviceInfo();
    return false;
}

window.onload = init;
