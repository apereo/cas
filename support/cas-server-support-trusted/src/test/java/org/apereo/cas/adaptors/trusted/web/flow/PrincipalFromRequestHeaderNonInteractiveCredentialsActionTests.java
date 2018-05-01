package org.apereo.cas.adaptors.trusted.web.flow;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.adaptors.trusted.config.TrustedAuthenticationConfiguration;
import org.jasig.cas.client.authentication.SimplePrincipal;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link PrincipalFromRequestHeaderNonInteractiveCredentialsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Import(TrustedAuthenticationConfiguration.class)
@Slf4j
public class PrincipalFromRequestHeaderNonInteractiveCredentialsActionTests extends AbstractCentralAuthenticationServiceTests {
    @Autowired
    @Qualifier("principalFromRemoteHeaderPrincipalAction")
    private Action action;

    @Test
    public void verifyRemoteUserExists() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        request.setUserPrincipal(new SimplePrincipal("casuser"));
        assertEquals("success", this.action.execute(context).getId());

        request.setRemoteUser("test");
        assertEquals("success", this.action.execute(context).getId());

        request.addHeader("principal", "casuser");
        assertEquals("success", this.action.execute(context).getId());
    }
}
