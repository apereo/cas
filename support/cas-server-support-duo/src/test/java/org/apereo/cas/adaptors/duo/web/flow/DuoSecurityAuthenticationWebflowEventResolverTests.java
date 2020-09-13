package org.apereo.cas.adaptors.duo.web.flow;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.adaptors.duo.BaseDuoSecurityTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DuoSecurityAuthenticationWebflowEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseDuoSecurityTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.duo[0].duo-secret-key=1234567890",
        "cas.authn.mfa.duo[0].duo-application-key=abcdefghijklmnop",
        "cas.authn.mfa.duo[0].duo-integration-key=QRSTUVWXYZ",
        "cas.authn.mfa.duo[0].duo-api-host=theapi.duosecurity.com"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("WebflowEvents")
public class DuoSecurityAuthenticationWebflowEventResolverTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("duoAuthenticationWebflowEventResolver")
    private CasWebflowEventResolver resolver;

    @Test
    public void verifyOperation() {
        val event = resolver.resolveSingle(BaseDuoSecurityTests.getMockRequestContext(applicationContext));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
    }

}
