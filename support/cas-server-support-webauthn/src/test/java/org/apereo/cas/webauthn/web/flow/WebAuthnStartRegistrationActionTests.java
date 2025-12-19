package org.apereo.cas.webauthn.web.flow;

import module java.base;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link WebAuthnStartRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowMfaActions")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class)
class WebAuthnStartRegistrationActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_WEB_AUTHN_START_REGISTRATION)
    private Action webAuthnStartRegistrationAction;

    @Autowired
    @Qualifier("webAuthnMultifactorAuthenticationProvider")
    private MultifactorAuthenticationProvider webAuthnMultifactorAuthenticationProvider;

    @Autowired
    @Qualifier("webAuthnProtocolEndpointConfigurer")
    private CasWebSecurityConfigurer<HttpSecurity> webAuthnProtocolEndpointConfigurer;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, webAuthnMultifactorAuthenticationProvider);

        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);
        assertNotNull(webAuthnStartRegistrationAction);
        assertNull(webAuthnStartRegistrationAction.execute(context));
        assertTrue(context.getFlowScope().contains(WebAuthnStartRegistrationAction.FLOW_SCOPE_WEB_AUTHN_APPLICATION_ID));
        assertTrue(context.getFlowScope().contains("displayName"));

        val http = new HttpSecurity(mock(ObjectPostProcessor.class),
            new AuthenticationManagerBuilder(mock(ObjectPostProcessor.class)),
            Map.of(ApplicationContext.class, applicationContext));
        assertNotNull(webAuthnProtocolEndpointConfigurer.configure(http));
        assertNotNull(http.getConfigurer(CsrfConfigurer.class));
    }

}
