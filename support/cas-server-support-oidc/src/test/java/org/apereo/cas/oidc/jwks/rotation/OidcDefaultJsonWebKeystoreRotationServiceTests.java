package org.apereo.cas.oidc.jwks.rotation;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeystoreRotationService;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OidcDefaultJsonWebKeystoreRotationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("OIDC")
@TestPropertySource(properties = "cas.authn.oidc.jwks.jwks-file=file:${#systemProperties['java.io.tmpdir']}/rotation.jwks")
public class OidcDefaultJsonWebKeystoreRotationServiceTests extends AbstractOidcTests {
    @Autowired
    @Qualifier("oidcJsonWebKeystoreRotationService")
    private OidcJsonWebKeystoreRotationService oidcJsonWebKeystoreRotationService;

    @BeforeAll
    public static void setup() {
        val keystore = new File(FileUtils.getTempDirectoryPath(), "rotation.jwks");
        if (keystore.exists()) {
            assertTrue(keystore.delete());
        }
    }

    @Test
    public void verifyOperation() throws Exception {
        var jwks = oidcJsonWebKeystoreRotationService.rotate();
        assertEquals(2, jwks.getJsonWebKeys().size());

        jwks = oidcJsonWebKeystoreRotationService.rotate();
        assertEquals(3, jwks.getJsonWebKeys().size());

        jwks = oidcJsonWebKeystoreRotationService.rotate();
        assertEquals(4, jwks.getJsonWebKeys().size());

        jwks = oidcJsonWebKeystoreRotationService.revoke();
        assertEquals(2, jwks.getJsonWebKeys().size());
    }

}
