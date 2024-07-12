package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.action.EventFactorySupport;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WebAuthnAuthenticationWebflowEventResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowEvents")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class)
class WebAuthnAuthenticationWebflowEventResolverTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("webAuthnAuthenticationWebflowEventResolver")
    private CasWebflowEventResolver webAuthnAuthenticationWebflowEventResolver;


    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        WebUtils.putCredential(context, RegisteredServiceTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "123456"));
        val event = webAuthnAuthenticationWebflowEventResolver.resolveSingle(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), context.getHttpServletResponse().getStatus());
        val support = new EventFactorySupport();
        assertTrue(event.getAttributes().contains(support.getExceptionAttributeName()));
    }

}
