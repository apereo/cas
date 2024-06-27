package org.apereo.cas.adaptors.yubikey.web.flow;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.adaptors.yubikey.BaseYubiKeyTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.action.EventFactorySupport;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link YubiKeyAuthenticationWebflowEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseYubiKeyTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.yubikey.client-id=18423",
        "cas.authn.mfa.yubikey.secret-key=zAIqhjui12mK8x82oe9qzBEb0As="
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("WebflowEvents")
@ExtendWith(CasTestExtension.class)
class YubiKeyAuthenticationWebflowEventResolverTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("yubikeyAuthenticationWebflowEventResolver")
    private CasWebflowEventResolver yubikeyAuthenticationWebflowEventResolver;

    @Test
    void verifyOperationFails() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val credential = RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "123456");
        WebUtils.putCredential(context, credential);
        val event = yubikeyAuthenticationWebflowEventResolver.resolveSingle(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), context.getHttpServletResponse().getStatus());
        val support = new EventFactorySupport();
        assertTrue(event.getAttributes().contains(support.getExceptionAttributeName()));
    }

}
