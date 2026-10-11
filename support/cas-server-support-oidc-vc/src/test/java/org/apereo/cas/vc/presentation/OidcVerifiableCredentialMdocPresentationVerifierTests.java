package org.apereo.cas.vc.presentation;

import module java.base;
import com.nimbusds.jose.jwk.JWK;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcVerifiableCredentialMdocPresentationVerifierTests}, which checks the handovers against the
 * examples of OpenID4VP 1.0, appendix B.2.6.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("OIDC")
class OidcVerifiableCredentialMdocPresentationVerifierTests {
    private static final String NONCE = "exc7gBkxjx1rdc9udRrveKvSsJIq80avlXeLHhGwqtA";

    private static final String ENCRYPTION_KEY = """
        {
          "kty": "EC",
          "crv": "P-256",
          "x": "DxiH5Q4Yx3UrukE2lWCErq8N8bqC9CHLLrAwLz5BmE0",
          "y": "XtLM4-3h5o3HUH0MHVJV0kyq0iBlrBwlh8qEDMZ4-Pc",
          "use": "enc",
          "alg": "ECDH-ES",
          "kid": "1"
        }
        """;

    @Test
    void verifyRedirectHandover() throws Throwable {
        val handover = OidcVerifiableCredentialMdocPresentationVerifier.buildRedirectHandover(
            "x509_san_dns:example.com", NONCE, thumbprint(), "https://example.com/response");
        assertEquals("82714f70656e494434565048616e646f7665725820048bc053c00442af9b8eed494c"
            + "efdd9d95240d254b046b11b68013722aad38ac", handover.encodeToHex().toLowerCase(Locale.ENGLISH));
    }

    @Test
    void verifyDigitalCredentialsHandover() throws Throwable {
        val handover = OidcVerifiableCredentialMdocPresentationVerifier.buildDigitalCredentialsHandover(
            "https://example.com", NONCE, thumbprint());
        assertEquals("82764f70656e4944345650444341504948616e646f7665725820fbece366f4212f97"
            + "62c74cfdbf83b8c69e371d5d68cea09cb4c48ca6daab761a", handover.encodeToHex().toLowerCase(Locale.ENGLISH));
    }

    private static byte[] thumbprint() throws Exception {
        val thumbprint = JWK.parse(ENCRYPTION_KEY).computeThumbprint().decode();
        assertEquals("4283ec927ae0f208daaa2d026a814f2b22dca52cf85ffa8f3f8626c6bd669047", HexFormat.of().formatHex(thumbprint));
        return thumbprint;
    }
}
