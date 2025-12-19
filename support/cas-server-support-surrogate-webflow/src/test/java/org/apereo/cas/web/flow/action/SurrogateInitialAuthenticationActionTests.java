package org.apereo.cas.web.flow.action;

import module java.base;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SurrogateInitialAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowAuthenticationActions")
@SpringBootTest(classes = BaseSurrogateAuthenticationTests.SharedTestConfiguration.class, properties = "cas.authn.surrogate.simple.surrogates.casuser=cassurrogate")
@ExtendWith(CasTestExtension.class)
class SurrogateInitialAuthenticationActionTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_SURROGATE_INITIAL_AUTHENTICATION)
    private Action initialAuthenticationAction;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyNoCredentialsFound() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        assertNull(initialAuthenticationAction.execute(context));
        assertFalse(WebUtils.hasSurrogateAuthenticationRequest(context));
    }

    @Test
    void verifySurrogateCredentialsFound() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val credential = new UsernamePasswordCredential();
        credential.setUsername("casuser");
        credential.assignPassword("Mellon");
        credential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait("cassurrogate"));
        WebUtils.putCredential(context, credential);
        assertNull(initialAuthenticationAction.execute(context));
    }

    @Test
    void verifySelectingSurrogateList() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val usernamePasswordCredential = new UsernamePasswordCredential();
        usernamePasswordCredential.setUsername("+casuser");
        usernamePasswordCredential.assignPassword("Mellon");
        WebUtils.putCredential(context, usernamePasswordCredential);
        assertNull(initialAuthenticationAction.execute(context));
        assertTrue(WebUtils.hasSurrogateAuthenticationRequest(context));
        val credential = WebUtils.getCredential(context);
        assertEquals("casuser", credential.getId());
        assertTrue(credential.getCredentialMetadata().getTrait(SurrogateCredentialTrait.class).isEmpty());
    }

    @Test
    void verifyUsernamePasswordCredentialsFound() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val c = new UsernamePasswordCredential();
        c.setUsername("cassurrogate+casuser");
        c.assignPassword("Mellon");
        WebUtils.putCredential(context, c);
        assertNull(initialAuthenticationAction.execute(context));
        assertFalse(WebUtils.hasSurrogateAuthenticationRequest(context));
        val credential = WebUtils.getCredential(context);
        assertEquals("casuser", credential.getId());
        assertEquals("cassurrogate", credential.getCredentialMetadata()
            .getTrait(SurrogateCredentialTrait.class).get().getSurrogateUsername());
    }

    @Test
    void verifyUsernamePasswordCredentialsBadPasswordAndCancelled() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        var credential = new UsernamePasswordCredential();
        credential.setUsername("cassurrogate+casuser");
        credential.assignPassword("badpassword");
        WebUtils.putCredential(context, credential);
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
