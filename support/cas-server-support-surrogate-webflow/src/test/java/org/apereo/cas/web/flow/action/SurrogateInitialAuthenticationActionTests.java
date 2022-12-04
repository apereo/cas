package org.apereo.cas.web.flow.action;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
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
@Tag("WebflowAuthenticationActions")
public class SurrogateInitialAuthenticationActionTests extends BaseSurrogateInitialAuthenticationActionTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_SURROGATE_INITIAL_AUTHENTICATION)
    private Action initialAuthenticationAction;

    @Test
    public void verifyNoCredentialsFound() throws Exception {
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(),
            new MockHttpServletResponse()));
        assertNull(initialAuthenticationAction.execute(context));
        assertFalse(WebUtils.hasSurrogateAuthenticationRequest(context));
    }

    @Test
    public void verifySurrogateCredentialsFound() throws Exception {
        val context = new MockRequestContext();
        val credential = new UsernamePasswordCredential();
        credential.setUsername("casuser");
        credential.assignPassword("Mellon");
        credential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait("cassurrogate"));
        WebUtils.putCredential(context, credential);
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));
        assertNull(initialAuthenticationAction.execute(context));
    }

    @Test
    public void verifySelectingSurrogateList() throws Exception {
        val context = new MockRequestContext();
        val c = new UsernamePasswordCredential();
        c.setUsername("+casuser");
        c.assignPassword("Mellon");
        WebUtils.putCredential(context, c);
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));
        assertNull(initialAuthenticationAction.execute(context));
        assertTrue(WebUtils.hasSurrogateAuthenticationRequest(context));
        val credential = WebUtils.getCredential(context);
        assertEquals("casuser", credential.getId());
        assertTrue(credential.getCredentialMetadata().getTrait(SurrogateCredentialTrait.class).isEmpty());
    }

    @Test
    public void verifyUsernamePasswordCredentialsFound() throws Exception {
        val context = new MockRequestContext();
        val c = new UsernamePasswordCredential();
        c.setUsername("cassurrogate+casuser");
        c.assignPassword("Mellon");
        WebUtils.putCredential(context, c);
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));
        assertNull(initialAuthenticationAction.execute(context));
        assertFalse(WebUtils.hasSurrogateAuthenticationRequest(context));
        val credential = WebUtils.getCredential(context);
        assertEquals("casuser", credential.getId());
        assertEquals("cassurrogate", credential.getCredentialMetadata()
            .getTrait(SurrogateCredentialTrait.class).get().getSurrogateUsername());
    }

    @Test
    public void verifyUsernamePasswordCredentialsBadPasswordAndCancelled() throws Exception {
        val context = new MockRequestContext();
        var credential = new UsernamePasswordCredential();
        credential.setUsername("cassurrogate+casuser");
        credential.assignPassword("badpassword");
        WebUtils.putCredential(context, credential);
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));
        assertNull(initialAuthenticationAction.execute(context));
        val credential1 = WebUtils.getCredential(context);
        assertEquals("casuser", credential1.getId());
        assertEquals("cassurrogate", credential.getCredentialMetadata()
            .getTrait(SurrogateCredentialTrait.class).get().getSurrogateUsername());

        val sc = WebUtils.getCredential(context, UsernamePasswordCredential.class);
        sc.setUsername("casuser");
        sc.assignPassword("Mellon");
        WebUtils.putCredential(context, sc);
        assertNull(initialAuthenticationAction.execute(context));
        val credential2 = WebUtils.getCredential(context);
        assertEquals("casuser", credential2.getId());
        assertTrue(credential.getCredentialMetadata().getTrait(SurrogateCredentialTrait.class).isEmpty());
    }
}
