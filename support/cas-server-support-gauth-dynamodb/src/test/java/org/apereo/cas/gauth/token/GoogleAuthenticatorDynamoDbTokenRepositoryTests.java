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
import org.apache.commons.lang3.time.StopWatch;
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
        "cas.authn.mfa.gauth.dynamo-db.region=us-east-1"
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

    @Test
    void verifyExpiredTokens() {
        val token = new GoogleAuthenticatorToken(1111, userId);
        token.setIssuedDateTime(LocalDateTime.now(ZoneOffset.UTC).plusHours(1));
        oneTimeTokenAuthenticatorTokenRepository.store(token);
        var t1 = oneTimeTokenAuthenticatorTokenRepository.get(userId, token.getToken());
        assertEquals(token, t1);
        oneTimeTokenAuthenticatorTokenRepository.clean();
        t1 = oneTimeTokenAuthenticatorTokenRepository.get(userId, t1.getToken());
        assertNull(t1);
    }

    @Test
    void verifyLargeDataSet() {
        val tokens = Stream.generate(() -> new GoogleAuthenticatorToken(Integer.valueOf(RandomUtils.randomNumeric(6)), userId)).limit(500);
        var stopwatch = new StopWatch();
        stopwatch.start();
        tokens.forEach(token -> {
            oneTimeTokenAuthenticatorTokenRepository.store(token);
            assertNotNull(oneTimeTokenAuthenticatorTokenRepository.get(userId, token.getToken()));
            oneTimeTokenAuthenticatorTokenRepository.remove(token.getToken());
        });
        stopwatch.stop();
        var time = stopwatch.getTime(TimeUnit.SECONDS);
        assertTrue(time <= 15);
    }
}
