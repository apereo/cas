package org.apereo.cas.oidc.federation;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcFederationDefaultJsonWebKeystoreServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("OIDCWeb")
public class OidcFederationDefaultJsonWebKeystoreServiceTests {

    @Nested
    @TestPropertySource(properties = {
        "CasFeatureModule.OpenIDConnect.federation.enabled=true",
        "cas.authn.oidc.federation.jwks-file=file:${#systemProperties['java.io.tmpdir']}/corrupted.jwks"
    })
    class KeystoreCorrupted extends AbstractOidcTests {
        @Autowired
        @Qualifier(OidcFederationJsonWebKeystoreService.BEAN_NAME)
        private OidcFederationJsonWebKeystoreService oidcFederationWebKeystoreService;

        @BeforeAll
        static void setup() throws Exception {
            val resource = new File(FileUtils.getTempDirectoryPath(), "corrupted.jwks");
            FileUtils.writeStringToFile(resource, "corrupted", StandardCharsets.UTF_8);
        }

        @Test
        void verifyOperation() throws Exception {
            val keystore = oidcFederationWebKeystoreService.toJWKSet();
            assertNotNull(keystore);
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "CasFeatureModule.OpenIDConnect.federation.enabled=true",
        "cas.authn.oidc.federation.jwks-file=file:${#systemProperties['java.io.tmpdir']}/keystore-missing.jwks"
    })
    class KeystoreMissing extends AbstractOidcTests {
        @Autowired
        @Qualifier(OidcFederationJsonWebKeystoreService.BEAN_NAME)
        private OidcFederationJsonWebKeystoreService oidcFederationWebKeystoreService;

        @BeforeAll
        static void setup() throws Exception {
            val resource = new File(FileUtils.getTempDirectoryPath(), "keystore-missing.jwks");
            if (resource.exists()) {
                FileUtils.delete(resource);
            }
        }

        @Test
        void verifyOperation() throws Exception {
            val keystore = oidcFederationWebKeystoreService.toJWKSet();
            assertNotNull(keystore);
            val jwksFile = SpringExpressionLanguageValueResolver.getInstance()
                .resolve(casProperties.getAuthn().getOidc().getFederation().getJwksFile());
            val resource = ResourceUtils.getRawResourceFrom(jwksFile);
            assertTrue(resource.exists());
        }
    }
}
