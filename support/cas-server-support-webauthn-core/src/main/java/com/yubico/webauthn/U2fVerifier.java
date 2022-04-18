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

package com.yubico.webauthn;

import com.yubico.data.RegistrationRequest;
import com.yubico.data.U2fRegistrationResponse;
import com.yubico.internal.util.CertificateParser;
import com.yubico.internal.util.ExceptionUtil;
import com.yubico.internal.util.JacksonCodecs;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.exception.Base64UrlException;
import com.yubico.webauthn.extension.appid.AppId;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;

public class U2fVerifier {

    public static boolean verify(
        final AppId appId, final RegistrationRequest request, final U2fRegistrationResponse response)
        throws CertificateException, IOException, Base64UrlException {
        var appIdHash = Crypto.sha256(appId.getId());
        var clientDataHash = Crypto.sha256(response.getCredential().getU2fResponse().getClientDataJSON());

        var clientData = JacksonCodecs.json()
            .readTree(response.getCredential().getU2fResponse().getClientDataJSON().getBytes());
        var challengeBase64 = clientData.get("challenge").textValue();

        ExceptionUtil.assure(
            request
                .getPublicKeyCredentialCreationOptions()
                .getChallenge()
                .equals(ByteArray.fromBase64Url(challengeBase64)),
            "Wrong challenge.");

        var attestationCertAndSignatureStream = new ByteArrayInputStream(
            response.getCredential().getU2fResponse().getAttestationCertAndSignature().getBytes());

        var attestationCert = CertificateParser.parseDer(attestationCertAndSignatureStream);

        var signatureBytes = new byte[attestationCertAndSignatureStream.available()];
        attestationCertAndSignatureStream.read(signatureBytes);
        var signature = new ByteArray(signatureBytes);

        return new U2fRawRegisterResponse(
            response.getCredential().getU2fResponse().getPublicKey(),
            response.getCredential().getU2fResponse().getKeyHandle(),
            attestationCert,
            signature)
            .verifySignature(appIdHash, clientDataHash);
    }

}
