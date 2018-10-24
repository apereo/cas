package org.apereo.cas.adaptors.trusted.web.flow;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class PrincipalFromRequestHeaderNonInteractiveCredentialsActionTests extends BaseNonInteractiveCredentialsActionTests {
    @Autowired
    @Qualifier("principalFromRemoteHeaderPrincipalAction")
    private Action action;

    @Test
    public void verifyRemoteUserExists() throws Exception {
        val request = new MockHttpServletRequest();
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        val principal = mock(Principal.class);
        when(principal.getName()).thenReturn("casuser");
        request.setUserPrincipal(principal);
        assertEquals("success", this.action.execute(context).getId());

        request.setRemoteUser("test");
        assertEquals("success", this.action.execute(context).getId());

        request.addHeader("principal", "casuser");
        assertEquals("success", this.action.execute(context).getId());
    }
}
