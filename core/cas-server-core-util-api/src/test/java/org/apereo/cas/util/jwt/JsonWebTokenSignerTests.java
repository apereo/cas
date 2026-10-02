package org.apereo.cas.util.jwt;

import module java.base;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.crypto.CertUtils;
import lombok.val;
import org.jooq.lambda.UncheckedException;
import org.jose4j.jwk.EcJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.AesKey;
import org.jose4j.keys.EllipticCurves;
import org.jose4j.lang.InvalidAlgorithmException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import static org.apereo.cas.util.junit.Assertions.assertThrowsWithRootCause;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link JsonWebTokenSignerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Utility")
class JsonWebTokenSignerTests {
    @Test
    void verifySignClaims() {
        val claims = new JwtClaims();
        claims.setSubject("casuser");
        claims.setIssuedAtToNow();
        claims.setClaim("uid", "casuser");
        claims.setExpirationTimeMinutesInTheFuture(1);

        val secret = EncodingUtils.generateJsonWebKey(512);
        val key = new AesKey(secret.getBytes(StandardCharsets.UTF_8));
        val result = JsonWebTokenSigner.builder()
            .key(key)
            .headers(Map.of("name", "value"))
            .algorithm(AlgorithmIdentifiers.HMAC_SHA512)
            .build()
            .sign(claims);
        assertNotNull(result);
    }

    @Test
    void verifyNotAllowedAlgorithm() {
        val secret = EncodingUtils.generateJsonWebKey(256);
        val key = new AesKey(secret.getBytes(StandardCharsets.UTF_8));

        assertThrowsWithRootCause(
            UncheckedException.class, InvalidAlgorithmException.class,
            () -> JsonWebTokenSigner.builder()
                .allowedAlgorithms(Set.of(AlgorithmIdentifiers.HMAC_SHA512))
                .key(key)
                .headers(Map.of("name", "value"))
                .algorithm(AlgorithmIdentifiers.HMAC_SHA256)
                .build()
                .sign("ThisIsATest".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void verifySignByteArray() {
        val secret = EncodingUtils.generateJsonWebKey(256);
        val key = new AesKey(secret.getBytes(StandardCharsets.UTF_8));

        val result = JsonWebTokenSigner.builder()
            .key(key)
            .headers(Map.of("name", "value"))
            .algorithm(AlgorithmIdentifiers.HMAC_SHA256)
            .build()
            .sign("ThisIsATest".getBytes(StandardCharsets.UTF_8));
        assertNotNull(result);
    }

    @Test
    void verifyDisallowNoneAlgorithm() {
        val result = JsonWebTokenSigner.builder()
            .headers(Map.of("name", "value"))
            .algorithm(AlgorithmIdentifiers.NONE)
            .allowedAlgorithms(Set.of(AlgorithmIdentifiers.NONE, AlgorithmIdentifiers.HMAC_SHA256))
            .build()
            .sign("ThisIsATest".getBytes(StandardCharsets.UTF_8));
        assertNotNull(result);
    }

    @Test
    void verifyCertificateChainHeader() throws Exception {
        val certificate = CertUtils.readCertificate(new ClassPathResource("x509.crt"));
        val key = EcJwkGenerator.generateJwk(EllipticCurves.P256);
        val claims = new JwtClaims();
        claims.setSubject("casuser");

        val withChain = new JsonWebSignature();
        withChain.setCompactSerialization(JsonWebTokenSigner.builder()
            .key(key.getPrivateKey())
            .algorithm(AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256)
            .certificateChain(List.of(certificate))
            .build()
            .sign(claims));
        withChain.setKey(key.getPublicKey());
        assertTrue(withChain.verifySignature());
        assertEquals(List.of(certificate), withChain.getCertificateChainHeaderValue());

        val withoutChain = new JsonWebSignature();
        withoutChain.setCompactSerialization(JsonWebTokenSigner.builder()
            .key(key.getPrivateKey())
            .algorithm(AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256)
            .build()
            .sign(claims));
        assertNull(withoutChain.getHeaders().getObjectHeaderValue("x5c"));
    }
}
