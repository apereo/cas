package org.apereo.cas.oidc.token;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.oidc.issuer.OidcDefaultIssuerService;
import org.apereo.cas.support.oauth.OAuth20Constants;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcIdTokenSigningAndEncryptionServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("OIDC")
@TestPropertySource(properties = {
    "cas.authn.oidc.discovery.id-token-signing-alg-values-supported=RS256,RS384,RS512",
    "cas.authn.oidc.discovery.id-token-encryption-encoding-values-supported=A128CBC-HS256,A192CBC-HS384,A256CBC-HS512,A128GCM,A192GCM,A256GCM"
})
public class OidcIdTokenSigningAndEncryptionServiceTests extends AbstractOidcTests {

    @Test
    public void verifyOperation() {
        val claims = getClaims();
        val result = oidcTokenSigningAndEncryptionService.encode(getOidcRegisteredService(), claims);
        assertNotNull(result);
    }

    @Test
    public void verifyWrongType() {
        assertFalse(oidcTokenSigningAndEncryptionService.shouldEncryptToken(getOAuthRegisteredService("1", "http://localhost/cas")));
        assertFalse(oidcTokenSigningAndEncryptionService.shouldSignToken(getOAuthRegisteredService("1", "http://localhost/cas")));
    }

    @Test
    public void verifySkipSigning() {
        val oidcRegisteredService = getOidcRegisteredService(false, false);
        val result = oidcTokenSigningAndEncryptionService.shouldSignToken(oidcRegisteredService);
        assertFalse(result);
    }

    @Test
    public void verifyValidationOperation() {
        val claims = getClaims();
        val oidcRegisteredService = getOidcRegisteredService(true, false);
        val result = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);
        val jwt = oidcTokenSigningAndEncryptionService.decode(result, Optional.of(oidcRegisteredService));
        assertNotNull(jwt);
    }

    @Test
    public void verifyDecodingFailureBadToken() {
        val oidcRegisteredService = getOidcRegisteredService(true, false);
        assertThrows(IllegalArgumentException.class,
            () -> oidcTokenSigningAndEncryptionService.decode("bad-token", Optional.of(oidcRegisteredService)));
    }

    @Test
    public void verifyDecodingFailureNoIssuer() {
        val oidcRegisteredService = getOidcRegisteredService(true, false);
        val claims = getClaims();
        claims.setIssuer(StringUtils.EMPTY);
        val result = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);
        assertThrows(IllegalArgumentException.class,
            () -> oidcTokenSigningAndEncryptionService.decode(result, Optional.of(oidcRegisteredService)));
    }

    @Test
    public void verifyDecodingFailureBadIssuer() {
        val oidcRegisteredService = getOidcRegisteredService(true, false);
        val claims = getClaims();
        claims.setIssuer("bad-issuer");
        val result2 = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);
        assertThrows(IllegalArgumentException.class,
            () -> oidcTokenSigningAndEncryptionService.decode(result2, Optional.of(oidcRegisteredService)));
    }

    @Test
    public void verifyDecodingFailureBadClient() {
        val oidcRegisteredService = getOidcRegisteredService(true, false);
        val claims = getClaims();
        claims.setStringClaim(OAuth20Constants.CLIENT_ID, StringUtils.EMPTY);
        val result3 = oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims);
        assertThrows(IllegalArgumentException.class,
            () -> oidcTokenSigningAndEncryptionService.decode(result3, Optional.of(oidcRegisteredService)));
    }

    @Test
    public void verifyNoneNotSupported() {
        val claims = getClaims();
        val oidcRegisteredService = getOidcRegisteredService();
        oidcRegisteredService.setIdTokenSigningAlg(AlgorithmIdentifiers.NONE);
        assertThrows(IllegalArgumentException.class, () -> oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims));
        oidcRegisteredService.setIdTokenSigningAlg(AlgorithmIdentifiers.RSA_USING_SHA256);
        oidcRegisteredService.setIdTokenEncryptionAlg(AlgorithmIdentifiers.NONE);
        assertThrows(IllegalArgumentException.class, () -> oidcTokenSigningAndEncryptionService.encode(oidcRegisteredService, claims));
    }

    @Test
    public void verifyNoneSupported() {
        val discovery = new OidcServerDiscoverySettings(casProperties.getAuthn().getOidc().getCore().getIssuer());
        discovery.setIdTokenSigningAlgValuesSupported(List.of(AlgorithmIdentifiers.NONE));
        discovery.setIdTokenEncryptionAlgValuesSupported(List.of(AlgorithmIdentifiers.NONE));
        val service = new OidcIdTokenSigningAndEncryptionService(oidcDefaultJsonWebKeystoreCache,
            oidcServiceJsonWebKeystoreCache,
            new OidcDefaultIssuerService(casProperties.getAuthn().getOidc()),
            discovery);

        val claims = getClaims();
        val oidcRegisteredService = getOidcRegisteredService();
        oidcRegisteredService.setIdTokenSigningAlg(AlgorithmIdentifiers.NONE);
        oidcRegisteredService.setIdTokenEncryptionAlg(AlgorithmIdentifiers.NONE);

        val result = service.encode(oidcRegisteredService, claims);
        assertNotNull(result);
    }


}
