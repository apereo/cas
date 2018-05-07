package org.apereo.cas.adaptors.trusted.web.flow;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.adaptors.trusted.config.TrustedAuthenticationConfiguration;
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

import java.security.Principal;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
        final var request = new MockHttpServletRequest();
        final var context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        final var principal = mock(Principal.class);
        when(principal.getName()).thenReturn("casuser");
        request.setUserPrincipal(principal);
        assertEquals("success", this.action.execute(context).getId());

        request.setRemoteUser("test");
        assertEquals("success", this.action.execute(context).getId());

        request.addHeader("principal", "casuser");
        assertEquals("success", this.action.execute(context).getId());
    }
}
