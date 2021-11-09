package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.notifications.sms.MockSmsSender;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DisplayBeforePasswordlessAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@TestPropertySource(properties = {
    "spring.mail.host=localhost",
    "spring.mail.port=25000",
    
    "cas.authn.passwordless.accounts.groovy.location=classpath:PasswordlessAccount.groovy"
})
@Tag("Mail")
@EnabledIfPortOpen(port = 25000)
@Import(DisplayBeforePasswordlessAuthenticationActionTests.PasswordlessAuthenticationActionTestConfiguration.class)
public class DisplayBeforePasswordlessAuthenticationActionTests extends BasePasswordlessAuthenticationActionTests {
    @TestConfiguration("PasswordlessAuthenticationActionTestConfiguration")
    public static class PasswordlessAuthenticationActionTestConfiguration {
        @Bean
        public SmsSender smsSender() {
            return new MockSmsSender();
        }
    }
    
    @Autowired
    @Qualifier("displayBeforePasswordlessAuthenticationAction")
    private Action displayBeforePasswordlessAuthenticationAction;

    @Autowired
    @Qualifier("passwordlessUserAccountStore")
    private PasswordlessUserAccountStore passwordlessUserAccountStore;

    @Test
    public void verifyAction() throws Exception {
        val context = new MockRequestContext();
        context.setCurrentEvent(new Event(this, "processing"));
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter("username", "casuser");
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS,
            displayBeforePasswordlessAuthenticationAction.execute(context).getId());
    }

    @Test
    public void verifyNoUser() throws Exception {
        val context = new MockRequestContext();
        context.setCurrentEvent(new Event(this, "processing"));
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertThrows(UnauthorizedServiceException.class, () -> displayBeforePasswordlessAuthenticationAction.execute(context));
    }

    @Test
    public void verifyUnknownUser() throws Exception {
        val context = new MockRequestContext();
        context.setCurrentEvent(new Event(this, "processing"));
        val request = new MockHttpServletRequest();
        request.addParameter("username", "unknown");
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertThrows(UnauthorizedServiceException.class, () -> displayBeforePasswordlessAuthenticationAction.execute(context));
    }

    @Test
    public void verifyError() throws Exception {
        val context = new MockRequestContext();
        val attributes = new LocalAttributeMap("error", new IllegalArgumentException("Bad account"));
        context.setCurrentEvent(new Event(this, "processing", attributes));
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        WebUtils.putPasswordlessAuthenticationAccount(context, passwordlessUserAccountStore.findUser("casuser").get());
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS,
            displayBeforePasswordlessAuthenticationAction.execute(context).getId());
    }
}
