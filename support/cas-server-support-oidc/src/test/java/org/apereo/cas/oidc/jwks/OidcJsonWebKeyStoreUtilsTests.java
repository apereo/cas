package org.apereo.cas.oidc.jwks;

import module java.base;
import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.util.MockWebServer;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwk.EcJwkGenerator;
import org.jose4j.jwk.EllipticCurveJsonWebKey;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.OctJwkGenerator;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jwk.Use;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.keys.EllipticCurves;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcJsonWebKeyStoreUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("OIDC")
class OidcJsonWebKeyStoreUtilsTests extends AbstractOidcTests {

    @Test
    void verifyKeySet() {
        val service = getOidcRegisteredService();
        service.setJwks(StringUtils.EMPTY);
        assertTrue(OidcJsonWebKeyStoreUtils.getJsonWebKeySet(service, applicationContext, Optional.of(OidcJsonWebKeyUsage.SIGNING)).isEmpty());
    }

    @Test
    void verifyBadSvc() {
        assertTrue(OidcJsonWebKeyStoreUtils.getJsonWebKeySet(
            null, applicationContext, Optional.of(OidcJsonWebKeyUsage.SIGNING)).isEmpty());
    }

    /**
     * Expressions are evaluated in keystore locations only; keys defined inline, which may come from the application
     * itself, are taken as they are, so that their key identifiers cannot read the environment of CAS.
     */
    @Test
    void verifyExpressionsInKeystoreLocationsOnly() throws Throwable {
        val keyId = "${#systemProperties['java.version']}";
        val key = RsaJwkGenerator.generateJwk(2048);
        key.setKeyId(keyId);
        key.setUse(Use.ENCRYPTION);
        val service = getOidcRegisteredService();
        service.setJwks(new JsonWebKeySet(key).toJson());
        val inline = OidcJsonWebKeyStoreUtils.getJsonWebKeySet(service, applicationContext, Optional.of(OidcJsonWebKeyUsage.ENCRYPTION));
        assertEquals(keyId, inline.orElseThrow().getJsonWebKeys().getFirst().getKeyId());

        val keystore = Files.createTempFile("keystore", ".jwks");
        try {
            key.setKeyId("located");
            Files.writeString(keystore, new JsonWebKeySet(key).toJson());
            service.setJwks("file:${#systemProperties['java.io.tmpdir']}/" + keystore.getFileName());
            val located = OidcJsonWebKeyStoreUtils.getJsonWebKeySet(service, applicationContext, Optional.of(OidcJsonWebKeyUsage.ENCRYPTION));
            assertEquals("located", located.orElseThrow().getJsonWebKeys().getFirst().getKeyId());
        } finally {
            Files.deleteIfExists(keystore);
        }
    }

    /**
     * Keys published at a URL are read from loopback and private addresses, never from a link-local address such as
     * the metadata service of a cloud provider, and redirects are not followed.
     */
    @Test
    void verifyKeysPublishedAtUrl() throws Throwable {
        val key = RsaJwkGenerator.generateJwk(2048);
        key.setKeyId("published");
        key.setUse(Use.SIGNATURE);
        val service = getOidcRegisteredService();
        try (val keysServer = new MockWebServer(false, new JsonWebKeySet(key).toJson());
             val redirectingServer = new MockWebServer(false, "{\"keys\":[]}")) {
            keysServer.start();
            redirectingServer.start();
            service.setJwks("http://localhost:%s/jwks".formatted(keysServer.getPort()));
            val published = OidcJsonWebKeyStoreUtils.getJsonWebKeySet(service, applicationContext, Optional.of(OidcJsonWebKeyUsage.SIGNING));
            assertEquals("published", published.orElseThrow().getJsonWebKeys().getFirst().getKeyId());

            redirectingServer.responseStatus(HttpStatus.FOUND);
            redirectingServer.headers(Map.of(HttpHeaders.LOCATION, "http://localhost:%s/jwks".formatted(keysServer.getPort())));
            service.setJwks("http://localhost:%s/jwks".formatted(redirectingServer.getPort()));
            assertTrue(OidcJsonWebKeyStoreUtils.getJsonWebKeySet(service, applicationContext, Optional.of(OidcJsonWebKeyUsage.SIGNING)).isEmpty());
        }
        service.setJwks("http://169.254.169.254/latest/jwks");
        assertTrue(OidcJsonWebKeyStoreUtils.getJsonWebKeySet(service, applicationContext, Optional.of(OidcJsonWebKeyUsage.SIGNING)).isEmpty());
    }

    @Test
    void verifyEmptyKeySet() {
        val service = getOidcRegisteredService();
        service.setJwks(new JsonWebKeySet(List.of()).toJson());
        assertTrue(OidcJsonWebKeyStoreUtils.getJsonWebKeySet(service, applicationContext, Optional.of(OidcJsonWebKeyUsage.SIGNING)).isEmpty());
    }

    @Test
    void verifyEc() throws Throwable {
        val p256 = OidcJsonWebKeyStoreUtils.generateJsonWebKey("ec", 256, OidcJsonWebKeyUsage.SIGNING);
        assertEquals(EllipticCurves.P_256, assertInstanceOf(EllipticCurveJsonWebKey.class, p256).getCurveName());
        assertEquals(AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256, p256.getAlgorithm());
        val p384 = OidcJsonWebKeyStoreUtils.generateJsonWebKey("ec", 384, OidcJsonWebKeyUsage.SIGNING);
        assertEquals(EllipticCurves.P_384, assertInstanceOf(EllipticCurveJsonWebKey.class, p384).getCurveName());
        assertEquals(AlgorithmIdentifiers.ECDSA_USING_P384_CURVE_AND_SHA384, p384.getAlgorithm());
        val p521 = OidcJsonWebKeyStoreUtils.generateJsonWebKey("ec", 512, OidcJsonWebKeyUsage.SIGNING);
        assertEquals(EllipticCurves.P_521, assertInstanceOf(EllipticCurveJsonWebKey.class, p521).getCurveName());
        assertEquals(AlgorithmIdentifiers.ECDSA_USING_P521_CURVE_AND_SHA512, p521.getAlgorithm());
        val encryption = OidcJsonWebKeyStoreUtils.generateJsonWebKey("ec", 256, OidcJsonWebKeyUsage.ENCRYPTION);
        assertEquals("enc", encryption.getUse());
        assertNull(encryption.getAlgorithm());
        assertEquals(AlgorithmIdentifiers.RSA_USING_SHA256, OidcJsonWebKeyStoreUtils.getSigningAlgorithm(RsaJwkGenerator.generateJwk(2048)));
        assertNull(OidcJsonWebKeyStoreUtils.getSigningAlgorithm(OctJwkGenerator.generateJwk(256)));
    }

    @Test
    void verifySigningAlgorithmCompatibility() throws Throwable {
        val ecKey = EcJwkGenerator.generateJwk(EllipticCurves.P256);
        assertTrue(OidcJsonWebKeyStoreUtils.isSigningAlgorithmCompatible(ecKey, AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256));
        assertFalse(OidcJsonWebKeyStoreUtils.isSigningAlgorithmCompatible(ecKey, AlgorithmIdentifiers.ECDSA_USING_P521_CURVE_AND_SHA512));
        assertFalse(OidcJsonWebKeyStoreUtils.isSigningAlgorithmCompatible(ecKey, AlgorithmIdentifiers.RSA_USING_SHA256));
        val rsaKey = RsaJwkGenerator.generateJwk(2048);
        assertTrue(OidcJsonWebKeyStoreUtils.isSigningAlgorithmCompatible(rsaKey, AlgorithmIdentifiers.RSA_PSS_USING_SHA256));
        assertFalse(OidcJsonWebKeyStoreUtils.isSigningAlgorithmCompatible(rsaKey, AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256));
    }

    @Test
    void verifyMismatchedEllipticCurveAlgorithmsAreCorrectedOnLoad() throws Throwable {
        val signing = EcJwkGenerator.generateJwk(EllipticCurves.P256);
        signing.setKeyId("signing");
        signing.setUse(Use.SIGNATURE);
        signing.setAlgorithm(AlgorithmIdentifiers.ECDSA_USING_P521_CURVE_AND_SHA512);
        val encryption = EcJwkGenerator.generateJwk(EllipticCurves.P256);
        encryption.setKeyId("encryption");
        encryption.setUse(Use.ENCRYPTION);
        encryption.setAlgorithm(AlgorithmIdentifiers.ECDSA_USING_P521_CURVE_AND_SHA512);
        val p384 = EcJwkGenerator.generateJwk(EllipticCurves.P384);
        p384.setKeyId("p384");
        p384.setAlgorithm(AlgorithmIdentifiers.ECDSA_USING_P384_CURVE_AND_SHA384);
        val rsa = RsaJwkGenerator.generateJwk(2048);
        rsa.setKeyId("rsa");
        rsa.setAlgorithm(AlgorithmIdentifiers.RSA_USING_SHA512);
        val json = new JsonWebKeySet(signing, encryption, p384, rsa).toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);

        val keystore = OidcJsonWebKeystoreGeneratorService.toJsonWebKeyStore(new ByteArrayResource(json.getBytes(StandardCharsets.UTF_8)));
        val correctedSigningKey = (PublicJsonWebKey) keystore.findJsonWebKey("signing", null, null, null);
        assertEquals(AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256, correctedSigningKey.getAlgorithm());
        assertNull(keystore.findJsonWebKey("encryption", null, null, null).getAlgorithm());
        assertEquals(AlgorithmIdentifiers.ECDSA_USING_P384_CURVE_AND_SHA384, keystore.findJsonWebKey("p384", null, null, null).getAlgorithm());
        assertEquals(AlgorithmIdentifiers.RSA_USING_SHA512, keystore.findJsonWebKey("rsa", null, null, null).getAlgorithm());

        val jws = new JsonWebSignature();
        jws.setPayload("payload");
        jws.setAlgorithmHeaderValue(correctedSigningKey.getAlgorithm());
        jws.setKey(correctedSigningKey.getPrivateKey());
        val verifier = new JsonWebSignature();
        verifier.setCompactSerialization(jws.getCompactSerialization());
        verifier.setKey(correctedSigningKey.getPublicKey());
        assertTrue(verifier.verifySignature());
    }

    @Test
    void verifyParsing() {
        val key = OidcJsonWebKeyStoreUtils.generateJsonWebKey("ec", 512, OidcJsonWebKeyUsage.SIGNING);
        val keyset = new JsonWebKeySet(key);
        assertNotNull(OidcJsonWebKeyStoreUtils.parseJsonWebKeySet(
            keyset.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE)));
    }
}
