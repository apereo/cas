package org.apereo.cas.web.flow.action;

import org.apereo.cas.authentication.SurrogateUsernamePasswordCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SurrogateInitialAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowActions")
public class SurrogateInitialAuthenticationActionTests extends BaseSurrogateInitialAuthenticationActionTests {

    @Autowired
    @Qualifier("authenticationViaFormAction")
    private Action authenticationViaFormAction;

    @Test
    public void verifyNoCredentialsFound() {
        try {
            val context = new MockRequestContext();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(),
                new MockHttpServletResponse()));
            assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, authenticationViaFormAction.execute(context).getId());
        } catch (final Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void verifySurrogateCredentialsFound() {
        try {
            val context = new MockRequestContext();
            val c = new SurrogateUsernamePasswordCredential();
            c.setUsername("casuser");
            c.setPassword("Mellon");
            c.setSurrogateUsername("cassurrogate");
            WebUtils.putCredential(context, c);
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, authenticationViaFormAction.execute(context).getId());
        } catch (final Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void verifyUsernamePasswordCredentialsFound() {
        try {
            val context = new MockRequestContext();
            val c = new UsernamePasswordCredential();
            c.setUsername("cassurrogate+casuser");
            c.setPassword("Mellon");
            WebUtils.putCredential(context, c);
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, authenticationViaFormAction.execute(context).getId());
            assertTrue(WebUtils.getCredential(context) instanceof UsernamePasswordCredential);
        } catch (final Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    public void verifyUsernamePasswordCredentialsBadPasswordAndCancelled() {
        try {
            val context = new MockRequestContext();
            var credential = new UsernamePasswordCredential();
            credential.setUsername("cassurrogate+casuser");
            credential.setPassword("badpassword");
            WebUtils.putCredential(context, credential);
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));
            assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, authenticationViaFormAction.execute(context).getId());
            assertTrue(WebUtils.getCredential(context) instanceof SurrogateUsernamePasswordCredential);

            val sc = WebUtils.getCredential(context, SurrogateUsernamePasswordCredential.class);
            sc.setUsername("casuser");
            sc.setPassword("Mellon");
            WebUtils.putCredential(context, sc);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, authenticationViaFormAction.execute(context).getId());
            assertTrue(WebUtils.getCredential(context) instanceof UsernamePasswordCredential);
        } catch (final Exception e) {
            throw new AssertionError(e);
        }
    }
}
