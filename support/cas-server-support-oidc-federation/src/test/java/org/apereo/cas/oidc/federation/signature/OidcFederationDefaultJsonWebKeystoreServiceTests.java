package org.apereo.cas.oidc.federation.signature;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.configuration.model.support.quartz.SchedulingProperties;
import org.apereo.cas.oidc.federation.AbstractOidcTrustAnchorFederationTests;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyRevocation;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityID;
import com.nimbusds.openid.connect.sdk.federation.entities.EntityStatementClaimsSet;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

/**
 * This is {@link OidcFederationDefaultJsonWebKeystoreServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("OIDCWeb")
class OidcFederationDefaultJsonWebKeystoreServiceTests {

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.federation.jwks-file=file:${#systemProperties['java.io.tmpdir']}/keystore-without-keys.jwks")
    class KeystoreWithoutKeys extends AbstractOidcTrustAnchorFederationTests {
        @Autowired
        @Qualifier(OidcFederationJsonWebKeystoreService.BEAN_NAME)
        private OidcFederationJsonWebKeystoreService oidcFederationWebKeystoreService;

        @BeforeAll
        static void setup() throws Exception {
            val resource = new File(FileUtils.getTempDirectoryPath(), "keystore-without-keys.jwks");
            FileUtils.writeStringToFile(resource, "{\"keys\":[]}", StandardCharsets.UTF_8);
        }

        @Test
        void verifyOperation() throws Exception {
            val keystore = oidcFederationWebKeystoreService.toJWKSet();
            assertFalse(keystore.getKeys().isEmpty());
            val stored = FileUtils.readFileToString(new File(FileUtils.getTempDirectoryPath(), "keystore-without-keys.jwks"), StandardCharsets.UTF_8);
            assertTrue(keystore.getKeys().stream().allMatch(key -> stored.contains(key.getKeyID())));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.oidc.federation.jwks-file=file:${#systemProperties['java.io.tmpdir']}/keystore-missing.jwks")
    class KeystoreMissing extends AbstractOidcTrustAnchorFederationTests {
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

    /**
     * Key rollover (OpenID Federation 1.0, section 11.2): a key is published before it signs and after it stopped
     * signing, and retired keys move to the historical keys (section 8.7), public only.
     */
    @Nested
    class KeystoreRotation {
        @TempDir
        private File directory;

        @Test
        void verifyRotationAndRevocation() throws Exception {
            val file = new File(directory, "federation.jwks");
            val service = newService(file);
            val initial = keyIds(service.toJWKSet());
            assertEquals(2, initial.size());
            assertEquals(initial.getFirst(), signingKeyId(service));

            val rotated = keyIds(service.rotate());
            assertEquals(3, rotated.size());
            assertTrue(rotated.containsAll(initial));
            assertEquals(initial.get(1), signingKeyId(service));

            val published = keyIds(service.revoke());
            assertEquals(2, published.size());
            assertFalse(published.contains(initial.getFirst()));
            val historical = service.getHistoricalKeys();
            assertEquals(List.of(initial.getFirst()), historical.stream().map(JWK::getKeyID).toList());
            assertNotNull(historical.getFirst().getExpirationTime());
            assertNotNull(historical.getFirst().getIssueTime());
            assertFalse(historical.getFirst().isPrivate());

            val reloaded = newService(file);
            assertEquals(published, keyIds(reloaded.toJWKSet()));
            assertEquals(1, reloaded.getHistoricalKeys().size());
            assertEquals(initial.get(1), signingKeyId(reloaded));
        }

        /**
         * A revoked key (section 8.7) leaves the published keys at once and is published as a revoked historical key; a
         * key already retired keeps its expiration and is marked revoked as well.
         */
        @Test
        void verifyRevocationOfCompromisedKeys() throws Exception {
            val file = new File(directory, "compromised.jwks");
            val service = newService(file);
            val initial = keyIds(service.toJWKSet());
            assertTrue(service.revokeKey("unknown", "compromised").isEmpty());

            val published = keyIds(service.revokeKey(initial.getFirst(), "compromised").orElseThrow());
            assertEquals(2, published.size());
            assertFalse(published.contains(initial.getFirst()));
            assertEquals(initial.get(1), signingKeyId(service));
            val revoked = service.getHistoricalKeys().getFirst();
            assertEquals(initial.getFirst(), revoked.getKeyID());
            assertEquals(KeyRevocation.Reason.COMPROMISED, revoked.getKeyRevocation().getReason());
            assertNotNull(revoked.getKeyRevocation().getRevocationTime());
            assertNotNull(revoked.getExpirationTime());
            assertFalse(revoked.isPrivate());

            service.rotate();
            service.revoke();
            val retired = findHistoricalKey(service, initial.get(1));
            assertNull(retired.getKeyRevocation());
            assertTrue(service.revokeKey(initial.get(1), null).isPresent());
            val revokedLater = findHistoricalKey(newService(file), initial.get(1));
            assertNotNull(revokedLater.getKeyRevocation());
            assertNull(revokedLater.getKeyRevocation().getReason());
            assertEquals(retired.getExpirationTime(), revokedLater.getExpirationTime());

            val legacyFile = new File(directory, "single.jwks");
            FileUtils.writeStringToFile(legacyFile, new JWKSet(new RSAKeyGenerator(2048).keyID("single").generate()).toString(false),
                StandardCharsets.UTF_8);
            val legacy = newService(legacyFile);
            assertEquals(2, keyIds(legacy.revokeKey("single", "superseded").orElseThrow()).size());
            assertNotEquals("single", signingKeyId(legacy));
        }

        @Test
        void verifyKeystoreWithoutFutureKeyRotatesInTwoSteps() throws Exception {
            val file = new File(directory, "legacy.jwks");
            FileUtils.writeStringToFile(file, new JWKSet(new RSAKeyGenerator(2048).keyID("legacy").generate()).toString(false),
                StandardCharsets.UTF_8);
            val service = newService(file);
            assertEquals(List.of("legacy"), keyIds(service.toJWKSet()));
            assertEquals(2, keyIds(service.rotate()).size());
            assertEquals("legacy", signingKeyId(service));
            service.rotate();
            assertNotEquals("legacy", signingKeyId(service));
            assertTrue(keyIds(service.toJWKSet()).contains("legacy"));
        }

        /**
         * A scheduled rotation makes a future key current only once it has been published long enough, and a scheduled
         * revocation retires a previous key only once it has been previous long enough, so that running them again, on
         * a restart or on another node, changes nothing.
         */
        @Test
        void verifyScheduledRotationAndRevocationWhenDue() throws Exception {
            val file = new File(directory, "scheduled.jwks");
            val service = newService(file);
            val initial = keyIds(service.toJWKSet());
            assertEquals(initial, keyIds(service.rotateWhenDue(Duration.ofDays(1))));
            assertEquals(initial.getFirst(), signingKeyId(service));

            assertEquals(3, keyIds(service.rotateWhenDue(Duration.ZERO)).size());
            assertEquals(initial.get(1), signingKeyId(service));
            assertEquals(3, keyIds(service.rotateWhenDue(Duration.ofDays(1))).size());
            assertEquals(initial.get(1), signingKeyId(service));

            assertTrue(keyIds(service.revokeWhenDue(Duration.ofDays(1))).contains(initial.getFirst()));
            assertTrue(service.getHistoricalKeys().isEmpty());
            assertFalse(keyIds(service.revokeWhenDue(Duration.ZERO)).contains(initial.getFirst()));
            assertEquals(List.of(initial.getFirst()), service.getHistoricalKeys().stream().map(JWK::getKeyID).toList());
        }

        /**
         * Nodes that share the keystore file sign with the keys another node rotated, and do not rotate them again.
         */
        @Test
        void verifyKeystoreChangedByAnotherNodeIsReloaded() throws Exception {
            val file = new File(directory, "shared.jwks");
            val first = newService(file);
            val second = newService(file);
            assertEquals(signingKeyId(first), signingKeyId(second));
            first.rotate();
            assertTrue(file.setLastModified(file.lastModified() + 1000));
            assertEquals(signingKeyId(first), signingKeyId(second));
            assertEquals(keyIds(first.toJWKSet()), keyIds(second.rotateWhenDue(Duration.ofDays(1))));
        }

        /**
         * Changes work on copies of the keys: keys that cannot be saved are not used, and the keys in use stay as they are.
         */
        @Test
        void verifyKeysThatCannotBeSavedAreNotUsed() throws Exception {
            val file = new File(directory, "unsaved.jwks");
            val service = newService(file);
            val published = keyIds(service.toJWKSet());
            val signingKey = signingKeyId(service);
            Files.delete(file.toPath());
            Files.createDirectory(file.toPath());
            assertThrows(Exception.class, service::rotate);
            assertThrows(Exception.class, () -> service.revokeKey(signingKey, "compromised"));
            assertEquals(published, keyIds(service.toJWKSet()));
            assertEquals(signingKey, signingKeyId(service));
        }

        /**
         * The keystore is replaced at once, keeping its permissions, and written where a symbolic link to it points, even
         * before the target exists; a new keystore is readable by its owner only.
         */
        @Test
        void verifyKeystoreIsReplacedKeepingItsLinkAndPermissions() throws Exception {
            assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));
            val target = new File(directory, "target.jwks");
            newService(target);
            assertEquals(PosixFilePermissions.fromString("rw-------"), Files.getPosixFilePermissions(target.toPath()));
            val dangling = Files.createSymbolicLink(new File(directory, "dangling.jwks").toPath(), new File(directory, "later.jwks").toPath());
            newService(dangling.toFile());
            assertTrue(Files.isSymbolicLink(dangling));
            assertTrue(new File(directory, "later.jwks").isFile());
            val permissions = PosixFilePermissions.fromString("rw-r-----");
            Files.setPosixFilePermissions(target.toPath(), permissions);
            val link = Files.createSymbolicLink(new File(directory, "link.jwks").toPath(), target.toPath());
            val service = newService(link.toFile());
            service.rotate();
            assertTrue(Files.isSymbolicLink(link));
            assertEquals(permissions, Files.getPosixFilePermissions(target.toPath()));
            assertEquals(keyIds(service.toJWKSet()), keyIds(newService(target).toJWKSet()));
        }

        @Test
        void verifyKeystoreThatCannotBeReadIsNotReplaced() throws Exception {
            val file = new File(directory, "corrupted.jwks");
            FileUtils.writeStringToFile(file, "corrupted", StandardCharsets.UTF_8);
            assertThrows(IllegalStateException.class, () -> newService(file));
            assertEquals("corrupted", FileUtils.readFileToString(file, StandardCharsets.UTF_8));

            assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));
            val unreadable = new File(directory, "unreadable.jwks");
            newService(unreadable);
            val keys = FileUtils.readFileToString(unreadable, StandardCharsets.UTF_8);
            Files.setPosixFilePermissions(unreadable.toPath(), PosixFilePermissions.fromString("-w-------"));
            try {
                assumeFalse(Files.isReadable(unreadable.toPath()));
                assertThrows(IllegalStateException.class, () -> newService(unreadable));
            } finally {
                Files.setPosixFilePermissions(unreadable.toPath(), PosixFilePermissions.fromString("rw-------"));
            }
            assertEquals(keys, FileUtils.readFileToString(unreadable, StandardCharsets.UTF_8));
        }

        @Test
        void verifyScheduledKeyAge() {
            assertEquals(Duration.ofDays(45), OidcFederationDefaultJsonWebKeystoreService.getScheduledKeyAge(
                new SchedulingProperties().setRepeatInterval("P90D")));
            val monthly = OidcFederationDefaultJsonWebKeystoreService.getScheduledKeyAge(
                new SchedulingProperties().setCronExpression("0 0 0 1 * *"));
            assertTrue(monthly.compareTo(Duration.ofDays(14)) >= 0 && monthly.compareTo(Duration.ofHours(372)) <= 0, monthly::toString);
        }

        private static OidcFederationDefaultJsonWebKeystoreService newService(final File file) throws Exception {
            val properties = new OidcProperties();
            properties.getFederation().setJwksFile("file:" + file.getAbsolutePath());
            return new OidcFederationDefaultJsonWebKeystoreService(properties);
        }

        private static JWK findHistoricalKey(final OidcFederationJsonWebKeystoreService service, final String keyId) {
            return service.getHistoricalKeys().stream().filter(key -> keyId.equals(key.getKeyID())).findFirst().orElseThrow();
        }

        private static List<String> keyIds(final JWKSet keys) {
            return keys.getKeys().stream().map(JWK::getKeyID).toList();
        }

        private static String signingKeyId(final OidcFederationJsonWebKeystoreService service) throws Exception {
            val entityId = new EntityID("https://federation.example.org");
            val now = Instant.now();
            val statement = service.signEntityStatement(new EntityStatementClaimsSet(entityId, entityId,
                Date.from(now), Date.from(now.plusSeconds(60)), service.toJWKSet()));
            assertNotNull(statement.verifySignatureOfSelfStatement());
            return statement.getSignedStatement().getHeader().getKeyID();
        }
    }
}

