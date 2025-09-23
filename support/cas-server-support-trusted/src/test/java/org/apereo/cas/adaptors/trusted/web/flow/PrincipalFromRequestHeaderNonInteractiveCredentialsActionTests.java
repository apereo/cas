package org.apereo.cas.adaptors.trusted.web.flow;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import java.security.Principal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PrincipalFromRequestHeaderNonInteractiveCredentialsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowActions")
@TestPropertySource(properties = "cas.authn.adaptive.policy.reject-ip-addresses=1.2.3.4")
class PrincipalFromRequestHeaderNonInteractiveCredentialsActionTests extends BaseNonInteractiveCredentialsActionTests {
    @Autowired
    @Qualifier("principalFromRemoteHeaderPrincipalAction")
    private PrincipalFromRequestExtractorAction action;

    @Test
    void verifyRemoteUserExists() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val principal = mock(Principal.class);
        when(principal.getName()).thenReturn("casuser");
        context.getHttpServletRequest().setUserPrincipal(principal);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, this.action.execute(context).getId());

        context.getHttpServletRequest().setRemoteUser("test");
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, this.action.execute(context).getId());

        context.addHeader("principal", "casuser");
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, this.action.execute(context).getId());
    }


    @Test
    void verifyError() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.getHttpServletRequest().setRemoteUser("xyz");
        context.setParameter(casProperties.getAuthn().getMfa().getTriggers().getHttp().getRequestParameter(), "mfa-whatever");
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, this.action.execute(context).getId());
    }

    @Test
    void verifyAdaptiveError() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        
        context.getHttpServletRequest().setRemoteUser("xyz");
        context.setRemoteAddr("1.2.3.4");
        context.setLocalAddr("1.2.3.4");
        context.withUserAgent();
        context.setParameter("geolocation", "1000,1000,1000,1000");
        context.setClientInfo();

        context.setParameter(casProperties.getAuthn().getMfa().getTriggers().getHttp().getRequestParameter(), "mfa-whatever");
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService());
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, this.action.execute(context).getId());
    }
}
