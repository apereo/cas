package org.apereo.cas.mfa;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.MultifactorAuthenticationFailedException;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.twilio.CasTwilioMultifactorTokenCredential;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasTwilioMultifactorAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@ExtendWith(CasTestExtension.class)
@Tag("MFAProvider")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTest(
    classes = BaseTwilioMultifactorTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.twilio.core.account-id=${#randomString8}",
        "cas.authn.mfa.twilio.core.token=${#randomString8}",
        "cas.authn.mfa.twilio.core.service-sid=${#randomString8}"
    })
class CasTwilioMultifactorAuthenticationHandlerTests {
    @Autowired
    @Qualifier("casTwilioMultifactorAuthenticationHandler")
    private AuthenticationHandler casTwilioMultifactorAuthenticationHandler;

    @Test
    void verifyOperation() {
        val principal = RegisteredServiceTestUtils.getPrincipal("casuser", Map.of("phoneNumber", List.of("+13247463546")));
        val authentication = RegisteredServiceTestUtils.getAuthentication(principal);

        val credential = new CasTwilioMultifactorTokenCredential();
        credential.setToken(UUID.randomUUID().toString());
        credential.getCredentialMetadata().putProperty(Authentication.class.getName(), authentication);
        assertTrue(casTwilioMultifactorAuthenticationHandler.supports(credential));
        assertTrue(casTwilioMultifactorAuthenticationHandler.supports(credential.getClass()));
        assertThrows(MultifactorAuthenticationFailedException.class,
            () -> casTwilioMultifactorAuthenticationHandler.authenticate(credential, RegisteredServiceTestUtils.getService()));
    }
}
