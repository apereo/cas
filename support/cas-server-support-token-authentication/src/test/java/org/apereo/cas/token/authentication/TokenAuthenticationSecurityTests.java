package org.apereo.cas.token.authentication;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.util.RandomUtils;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TokenAuthenticationSecurityTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Authentication")
class TokenAuthenticationSecurityTests {
    private static final String SIGNING_SECRET = RandomUtils.randomAlphanumeric(256);

    private static final String ENCRYPTION_SECRET = RandomUtils.randomAlphanumeric(48);

    @Test
    void verifyRsaOperation() {
        val registeredService = newRegisteredService(Map.of(
            RegisteredServiceProperties.TOKEN_SECRET_SIGNING, "classpath:/RSA4096Private.key",
            RegisteredServiceProperties.TOKEN_SECRET_SIGNING_ALG, JWSAlgorithm.RS512.getName(),
            RegisteredServiceProperties.TOKEN_SECRET_ENCRYPTION, "classpath:/RSA4096Public.key",
            RegisteredServiceProperties.TOKEN_SECRET_ENCRYPTION_ALG, JWEAlgorithm.RSA_OAEP_256.getName(),
            RegisteredServiceProperties.TOKEN_SECRET_ENCRYPTION_METHOD, EncryptionMethod.A256CBC_HS512.getName()));
        val security = TokenAuthenticationSecurity.forRegisteredService(registeredService);
        val token = security.generateTokenFor(CoreAuthenticationTestUtils.getAuthentication("casuser", Map.of("department", List.<Object>of("CAS"))))
            .orElseThrow();
        val authenticatedToken = security.validateToken(token).orElseThrow();
        assertEquals("casuser", authenticatedToken.subject());
        assertEquals(List.of("CAS"), authenticatedToken.claims().get("department"));
    }

    @Test
    void verifySecretOperation() throws Exception {
        val security = TokenAuthenticationSecurity.forRegisteredService(newRegisteredService(signingAndEncryptionSecrets()));
        val token = security.generateTokenFor(CoreAuthenticationTestUtils.getAuthentication("casuser", Map.of("department", List.<Object>of("CAS"))))
            .orElseThrow();
        val header = JWEObject.parse(token).getHeader();
        assertEquals(JWEAlgorithm.DIR, header.getAlgorithm());
        assertEquals(EncryptionMethod.A192CBC_HS384, header.getEncryptionMethod());
        assertEquals("JWT", header.getContentType());
        val authenticatedToken = security.validateToken(token).orElseThrow();
        assertEquals("casuser", authenticatedToken.subject());
        assertEquals(List.of("CAS"), authenticatedToken.claims().get("department"));
        assertFalse(authenticatedToken.claims().containsKey("sub"));
    }

    @Test
    void verifyTokenBuiltByPac4j() throws Exception {
        val claims = new JWTClaimsSet.Builder()
            .subject("org.pac4j.core.profile.CommonProfile#casuser")
            .issueTime(new Date())
            .claim("uid", "casuser")
            .claim("address", Map.of("city", "Paris", "geo", Map.of("lat", "48.85")))
            .claim("$int_roles", List.of())
            .claim("$int_perms", List.of())
            .build();
        val token = encrypt(sign(claims, SIGNING_SECRET));
        val security = TokenAuthenticationSecurity.forRegisteredService(newRegisteredService(signingAndEncryptionSecrets()));
        val authenticatedToken = security.validateToken(token).orElseThrow();
        assertEquals("casuser", authenticatedToken.subject());
        assertEquals("casuser", authenticatedToken.claims().get("uid"));
        assertEquals("Paris", authenticatedToken.claims().get("city"));
        assertEquals(Map.of("lat", "48.85"), authenticatedToken.claims().get("geo"));
        assertFalse(authenticatedToken.claims().containsKey("lat"));
        assertEquals(Map.of("city", "Paris", "geo", Map.of("lat", "48.85")), authenticatedToken.claims().get("address"));
        assertFalse(authenticatedToken.claims().containsKey("$int_roles"));
        assertFalse(authenticatedToken.claims().containsKey("$int_perms"));
        assertNotNull(authenticatedToken.claims().get("iat"));
    }

    @Test
    void verifyTokenOfPuppeteerScenario() {
        val token = "eyJjdHkiOiJKV1QiLCJlbmMiOiJBMTkyQ0JDLUhTMzg0IiwiYWxnIjoiZGlyIn0..zTe4qLwuyvVi6gHmSL4VcQ."
            + "NAmUxtxfEc-xJ0BO1DIUtxRN05-XTiB4bFVqN4YFvBPtVUrppTR5oXVKszWYQD_jWuhHnUBCVcvOBri9n-q6rKXQBkMnW9TLXQ4d9waJUOKpxORvgX3T56qoVfYTbUkVxe-5"
            + "VchX000JWy8GdhpyWawldG0au03GhU7jhVnQeMlb7WWaNFOGXQwx6wvF0B30UL-6wgZO1nWD7InaQXJiFZazE0HK0DX61DUbP6PFYJKOBkbSWg9vSSzCeTxVFx9uJXMSAg9_"
            + "vacpAYmq02ixV8e73CU9_hHhiCqEOYGunzwO4mEm6mn3fhPz6Q5azzPhZTc-lROKc66bmo_Y7jtMRmwgwBTq4diY9bIhw0x_qMsLLEdk1qk-dH9_FhBcunW2PkM8rCyGyDQ2"
            + "slL-Axs_zg.HgP2g-UNsljcDhO74OMFWKxkYiSo4mbK";
        val security = TokenAuthenticationSecurity.forRegisteredService(newRegisteredService(Map.of(
            RegisteredServiceProperties.TOKEN_SECRET_SIGNING, "V2x4dPVYmPT3v1YBf976B0dpFMYrfSwMiFUioYu69W6WyjdmqAkV6LgtpAhuhE3x7k3hKGsecnQFtgseOpvk7x5"
                + "sCE6jUt1d9fUfuiUCp6r2dvbiGmZedsQebd3daktu7x1MUGOxeTRgbv3XHK4eRlbfgsakg5cryHqUjaWR99yyBU3mHfnm30aC77z5pKpxnfQbhGy5pX9zITcgu27wWiTCcT"
                + "NHxzEkTBvsJek9Apz9wjRnEn65q9rLrps9MzYd",
            RegisteredServiceProperties.TOKEN_SECRET_ENCRYPTION, "oY233692DZea4fUfJxUhGJOp96rpCi0Tna7xl9vFxr4bZ60D")));
        val authenticatedToken = security.validateToken(token).orElseThrow();
        assertEquals("casuser", authenticatedToken.subject());
        assertTrue(authenticatedToken.claims().keySet().stream().noneMatch(name -> name.startsWith("$int_")));
    }

    @Test
    void verifyTypedSubjectOutsidePac4jIsKept() throws Exception {
        val security = TokenAuthenticationSecurity.forRegisteredService(newRegisteredService(signingAndEncryptionSecrets()));
        val token = sign(new JWTClaimsSet.Builder().subject("urn:example#casuser").build(), SIGNING_SECRET).serialize();
        assertEquals("urn:example#casuser", security.validateToken(token).orElseThrow().subject());
    }

    @Test
    void verifyUnsignedTokenIsRefused() throws Exception {
        val security = TokenAuthenticationSecurity.forRegisteredService(newRegisteredService(signingAndEncryptionSecrets()));
        val claims = new JWTClaimsSet.Builder().subject("casuser").build();
        assertTrue(security.validateToken(new PlainJWT(claims).serialize()).isEmpty());
        val encryptedJwt = new EncryptedJWT(new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A192CBC_HS384), claims);
        encryptedJwt.encrypt(new DirectEncrypter(ENCRYPTION_SECRET.getBytes(StandardCharsets.UTF_8)));
        assertTrue(security.validateToken(encryptedJwt.serialize()).isEmpty());
    }

    @Test
    void verifyServiceWithoutSigningSecret() throws Exception {
        val security = TokenAuthenticationSecurity.forRegisteredService(newRegisteredService(
            Map.of(RegisteredServiceProperties.TOKEN_SECRET_ENCRYPTION, ENCRYPTION_SECRET)));
        assertTrue(security.generateTokenFor(CoreAuthenticationTestUtils.getAuthentication("casuser")).isEmpty());
        val claims = new JWTClaimsSet.Builder().subject("casuser").build();
        assertTrue(security.validateToken(new PlainJWT(claims).serialize()).isEmpty());
        assertTrue(security.validateToken(sign(claims, RandomUtils.randomAlphanumeric(256)).serialize()).isEmpty());
        assertTrue(security.validateToken(encrypt(sign(claims, RandomUtils.randomAlphanumeric(256)))).isEmpty());

        val noSecrets = TokenAuthenticationSecurity.forRegisteredService(newRegisteredService(Map.of()));
        assertTrue(noSecrets.generateTokenFor(CoreAuthenticationTestUtils.getAuthentication("casuser")).isEmpty());
        assertTrue(noSecrets.validateToken(new PlainJWT(claims).serialize()).isEmpty());
    }

    @Test
    void verifyInvalidTokens() throws Exception {
        val security = TokenAuthenticationSecurity.forRegisteredService(newRegisteredService(signingAndEncryptionSecrets()));
        val claims = new JWTClaimsSet.Builder().subject("casuser").build();
        assertTrue(security.validateToken(encrypt(sign(claims, RandomUtils.randomAlphanumeric(256)))).isEmpty());
        val expired = new JWTClaimsSet.Builder().subject("casuser").expirationTime(Date.from(Instant.now().minusSeconds(60))).build();
        assertTrue(security.validateToken(encrypt(sign(expired, SIGNING_SECRET))).isEmpty());
        assertTrue(security.validateToken(encrypt(sign(new JWTClaimsSet.Builder().claim("uid", "casuser").build(), SIGNING_SECRET))).isEmpty());
        val typedBlankSubject = new JWTClaimsSet.Builder().subject("org.pac4j.core.profile.CommonProfile#  ").build();
        assertTrue(security.validateToken(encrypt(sign(typedBlankSubject, SIGNING_SECRET))).isEmpty());
        assertTrue(security.validateToken("not-a-token").isEmpty());
    }

    private static Map<RegisteredServiceProperties, String> signingAndEncryptionSecrets() {
        return Map.of(RegisteredServiceProperties.TOKEN_SECRET_SIGNING, SIGNING_SECRET,
            RegisteredServiceProperties.TOKEN_SECRET_ENCRYPTION, ENCRYPTION_SECRET);
    }

    private static SignedJWT sign(final JWTClaimsSet claims, final String secret) throws Exception {
        val jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(new MACSigner(secret.getBytes(StandardCharsets.UTF_8)));
        return jwt;
    }

    private static String encrypt(final SignedJWT jwt) throws Exception {
        val jwe = new JWEObject(new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A192CBC_HS384).contentType("JWT").build(),
            new Payload(jwt));
        jwe.encrypt(new DirectEncrypter(ENCRYPTION_SECRET.getBytes(StandardCharsets.UTF_8)));
        return jwe.serialize();
    }

    private static CasRegisteredService newRegisteredService(final Map<RegisteredServiceProperties, String> properties) {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService(RegisteredServiceTestUtils.CONST_TEST_URL);
        registeredService.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy());
        properties.forEach((name, value) -> {
            val property = new DefaultRegisteredServiceProperty();
            property.addValue(value);
            registeredService.getProperties().put(name.getPropertyName(), property);
        });
        return registeredService;
    }
}
