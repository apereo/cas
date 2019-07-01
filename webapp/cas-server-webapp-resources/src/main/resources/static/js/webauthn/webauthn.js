// Copyright (c) 2018, Yubico AB
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice, this
//    list of conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright notice,
//    this list of conditions and the following disclaimer in the documentation
//    and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

(function(root, factory) {
    if (typeof define === 'function' && define.amd) {
        define(['base64url'], factory);
    } else if (typeof module === 'object' && module.exports) {
        module.exports = factory(require('base64url'));
    } else {
        root.webauthn = factory(root.base64url);
    }
})(this, function(base64url) {

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
