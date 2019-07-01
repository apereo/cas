package com.yubico.webauthn;

import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.extension.appid.AppId;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.internal.util.CertificateParser;
import com.yubico.internal.util.ExceptionUtil;
import com.yubico.internal.util.WebAuthnCodecs;

import lombok.experimental.UtilityClass;
import lombok.val;

import org.apereo.cas.webauthn.registration.WebAuthnCredentialRegistrationResponse;
import org.apereo.cas.webauthn.registration.WebAuthnRegistrationRequest;

import java.io.ByteArrayInputStream;

/**
 * This is {@link WebAuthnVerifier}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@UtilityClass
public class WebAuthnVerifier {
    private static final ObjectMapper MAPPER = WebAuthnCodecs.json().findAndRegisterModules();
    private static final BouncyCastleCrypto CRYPTO = new BouncyCastleCrypto();

    public static boolean verify(final AppId appId, final WebAuthnRegistrationRequest request,
                                 final WebAuthnCredentialRegistrationResponse response) throws Exception {
        val appIdHash = CRYPTO.hash(appId.getId());
        val u2fResponse = response.getCredential().getU2fResponse();
        val clientDataHash = CRYPTO.hash(u2fResponse.getClientDataJson());

        val clientData = MAPPER.readTree(u2fResponse.getClientDataJson().getBytes());
        val challengeBase64 = clientData.get("challenge").textValue();
        val challenge = ByteArray.fromBase64Url(challengeBase64);

        ExceptionUtil.assure(
            request.getPublicKeyCredentialCreationOptions().getChallenge().equals(challenge),
            "Wrong challenge."
        );

        val attestationCertAndSignatureStream = new ByteArrayInputStream(u2fResponse.getAttestationCertAndSignature().getBytes());
        val attestationCert = CertificateParser.parseDer(attestationCertAndSignatureStream);
        val signatureBytes = new byte[attestationCertAndSignatureStream.available()];
        val count = attestationCertAndSignatureStream.read(signatureBytes);
        if (count > 0) {
            val signature = new ByteArray(signatureBytes);

            val u2fRawRegisterResponse = new U2fRawRegisterResponse(
                u2fResponse.getPublicKey(),
                u2fResponse.getKeyHandle(),
                attestationCert,
                signature
            );
            return u2fRawRegisterResponse.verifySignature(
                appIdHash,
                clientDataHash
            );
        }
        return false;
    }

}
