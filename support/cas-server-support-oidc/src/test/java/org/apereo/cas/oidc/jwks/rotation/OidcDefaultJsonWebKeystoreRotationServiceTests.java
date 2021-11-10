package org.apereo.cas.oidc.jwks.rotation;

import org.apereo.cas.oidc.AbstractOidcTests;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeystoreRotationService;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.jose4j.jwk.JsonWebKeySet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
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
public class OidcDefaultJsonWebKeystoreRotationServiceTests {
    static {
        try {
            val keystore = new File(FileUtils.getTempDirectoryPath(), "rotation.jwks");
            if (keystore.exists()) {
                assertTrue(keystore.delete());
            }
            val current = new File(FileUtils.getTempDirectoryPath(), "current.jwks");
            current.delete();
            FileUtils.copyFile(new ClassPathResource("current.jwks").getFile(), current);
        } catch (final Exception e) {
            fail(e);
        }
    }

    private static long countPreviousKeys(final JsonWebKeySet jwks) {
        return jwks.getJsonWebKeys()
            .stream()
            .filter(key -> OidcJsonWebKeystoreRotationService.JsonWebKeyLifecycleStates.getJsonWebKeyState(key).isPrevious())
            .count();
    }

    private static long countCurrentKeys(final JsonWebKeySet jwks) {
        return jwks.getJsonWebKeys()
            .stream()
            .filter(key -> OidcJsonWebKeystoreRotationService.JsonWebKeyLifecycleStates.getJsonWebKeyState(key).isCurrent())
            .count();
    }

    private static long countFutureKeys(final JsonWebKeySet jwks) {
        return jwks.getJsonWebKeys()
            .stream()
            .filter(key -> OidcJsonWebKeystoreRotationService.JsonWebKeyLifecycleStates.getJsonWebKeyState(key).isFuture())
            .count();
    }

    @TestPropertySource(properties = "cas.authn.oidc.jwks.jwks-file=file:${#systemProperties['java.io.tmpdir']}/rotation.jwks")
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class EmptyKeystoreTests extends AbstractOidcTests {

        @Test
        public void verifyOperation() throws Exception {
            var jwks = oidcJsonWebKeystoreRotationService.rotate();
            assertEquals(3, jwks.getJsonWebKeys().size());

            assertEquals(1, countCurrentKeys(jwks));
            assertEquals(1, countFutureKeys(jwks));
            assertEquals(1, countPreviousKeys(jwks));

            jwks = oidcJsonWebKeystoreRotationService.rotate();
            assertEquals(4, jwks.getJsonWebKeys().size());
            assertEquals(1, countCurrentKeys(jwks));
            assertEquals(1, countFutureKeys(jwks));
            assertEquals(2, countPreviousKeys(jwks));

            jwks = oidcJsonWebKeystoreRotationService.rotate();
            assertEquals(5, jwks.getJsonWebKeys().size());
            assertEquals(1, countCurrentKeys(jwks));
            assertEquals(1, countFutureKeys(jwks));
            assertEquals(3, countPreviousKeys(jwks));

            jwks = oidcJsonWebKeystoreRotationService.revoke();
            assertEquals(2, jwks.getJsonWebKeys().size());
            assertEquals(1, countCurrentKeys(jwks));
            assertEquals(1, countFutureKeys(jwks));
            assertEquals(0, countPreviousKeys(jwks));
        }
    }

    @TestPropertySource(properties = "cas.authn.oidc.jwks.jwks-file=file:${#systemProperties['java.io.tmpdir']}/current.jwks")
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class ExistingKeystoreTests extends AbstractOidcTests {

        @Test
        public void verifyOperation() throws Exception {
            var jwks = oidcJsonWebKeystoreRotationService.rotate();
            assertEquals(3, jwks.getJsonWebKeys().size());
        }
    }
}
