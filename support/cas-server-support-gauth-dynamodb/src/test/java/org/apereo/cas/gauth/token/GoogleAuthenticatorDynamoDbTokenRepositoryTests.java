package org.apereo.cas.gauth.token;

import module java.base;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasGoogleAuthenticatorAutoConfiguration;
import org.apereo.cas.config.CasGoogleAuthenticatorDynamoDbAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import software.amazon.awssdk.core.SdkSystemSetting;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleAuthenticatorDynamoDbTokenRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("DynamoDb")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasGoogleAuthenticatorDynamoDbAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasGoogleAuthenticatorAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasCoreWebflowAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class
},
    properties = {
        "cas.authn.mfa.gauth.dynamo-db.endpoint=http://localhost:8000",
        "cas.authn.mfa.gauth.dynamo-db.drop-tables-on-startup=true",
        "cas.authn.mfa.gauth.dynamo-db.local-instance=true",
        "cas.authn.mfa.gauth.dynamo-db.region=us-east-1",
        "cas.authn.mfa.gauth.dynamo-db.table-name=TokenRepositoryCredentialTests",
        "cas.authn.mfa.gauth.dynamo-db.token-table-name=TokenRepositoryTests"
    })
@EnableTransactionManagement(proxyTargetClass = false)
@EnableAspectJAutoProxy(proxyTargetClass = false)
@EnableScheduling
@Getter
@EnabledIfListeningOnPort(port = 8000)
class GoogleAuthenticatorDynamoDbTokenRepositoryTests extends BaseOneTimeTokenRepositoryTests {
    static {
        System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), "AKIAIPPIGGUNIO74C63Z");
        System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), "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");
    }

    /**
     * Cleaning must remove what has aged out of the expiry window and leave alone what has not.
     * The second half carries the weight: a cleaner comparing the wrong way round still passes a
     * test that only checks the old token is gone, while quietly deleting every token still in use.
     * Both tokens belong to this test's own user, so the repository-wide sweep is observed only
     * through records it owns.
     */
    @Test
    void verifyExpiredTokens() {
        val expired = new GoogleAuthenticatorToken(1111, userId);
        expired.setIssuedDateTime(LocalDateTime.now(ZoneOffset.UTC).minusDays(1));
        oneTimeTokenAuthenticatorTokenRepository.store(expired);
        assertNotNull(oneTimeTokenAuthenticatorTokenRepository.get(userId, expired.getToken()));

        val current = new GoogleAuthenticatorToken(2222, userId);
        current.setIssuedDateTime(LocalDateTime.now(ZoneOffset.UTC));
        oneTimeTokenAuthenticatorTokenRepository.store(current);
        assertNotNull(oneTimeTokenAuthenticatorTokenRepository.get(userId, current.getToken()));

        oneTimeTokenAuthenticatorTokenRepository.clean();
        assertNull(oneTimeTokenAuthenticatorTokenRepository.get(userId, expired.getToken()));
        assertNotNull(oneTimeTokenAuthenticatorTokenRepository.get(userId, current.getToken()));
    }

    /**
     * Five hundred tokens must each store, read back and remove cleanly. How long that takes is no
     * longer asserted: it was a measure of what the machine and the backend could manage rather
     * than of whether the repository is correct, and it answers to whatever else is running at the
     * same time.
     */
    @Test
    void verifyLargeDataSet() {
        val tokens = Stream.generate(() -> new GoogleAuthenticatorToken(Integer.valueOf(RandomUtils.randomNumeric(6)), userId)).limit(500);
        tokens.forEach(token -> {
            oneTimeTokenAuthenticatorTokenRepository.store(token);
            assertNotNull(oneTimeTokenAuthenticatorTokenRepository.get(userId, token.getToken()));
            oneTimeTokenAuthenticatorTokenRepository.remove(token.getToken());
        });
    }
}
