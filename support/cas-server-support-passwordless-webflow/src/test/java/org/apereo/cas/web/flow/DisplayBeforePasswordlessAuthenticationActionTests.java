package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessRequestParser;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.impl.token.PasswordlessAuthenticationToken;
import org.apereo.cas.notifications.sms.MockSmsSender;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
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
@EnabledIfListeningOnPort(port = 25000)
@Import(DisplayBeforePasswordlessAuthenticationActionTests.PasswordlessAuthenticationActionTestConfiguration.class)
class DisplayBeforePasswordlessAuthenticationActionTests extends BasePasswordlessAuthenticationActionTests {
    @TestConfiguration(value = "PasswordlessAuthenticationActionTestConfiguration", proxyBeanMethods = false)
    static class PasswordlessAuthenticationActionTestConfiguration {
        @Bean
        public SmsSender smsSender() {
            return MockSmsSender.INSTANCE;
        }
    }
    
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DISPLAY_BEFORE_PASSWORDLESS_AUTHN)
    private Action displayBeforePasswordlessAuthenticationAction;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_CREATE_PASSWORDLESS_AUTHN_TOKEN)
    private Action createPasswordlessAuthenticationTokenAction;

    @Autowired
    @Qualifier(PasswordlessUserAccountStore.BEAN_NAME)
    private PasswordlessUserAccountStore passwordlessUserAccountStore;

    @Test
    void verifyAction() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setCurrentEvent(new Event(this, "processing"));
        context.setParameter(PasswordlessRequestParser.PARAMETER_USERNAME, "casuser");
        assertEquals(CasWebflowConstants.TRANSITION_ID_CREATE,
            displayBeforePasswordlessAuthenticationAction.execute(context).getId());
        val result = createPasswordlessAuthenticationTokenAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        val token = result.getAttributes().get("result", PasswordlessAuthenticationToken.class);
        assertNotNull(token);
    }

    @Test
    void verifyNoUser() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setCurrentEvent(new Event(this, "processing"));
        assertThrows(UnauthorizedServiceException.class, () -> displayBeforePasswordlessAuthenticationAction.execute(context));
    }

    @Test
    void verifyUnknownUser() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setCurrentEvent(new Event(this, "processing"));
        context.setParameter(PasswordlessRequestParser.PARAMETER_USERNAME, "unknown");
        assertThrows(UnauthorizedServiceException.class, () -> displayBeforePasswordlessAuthenticationAction.execute(context));
    }

    @Test
    void verifyError() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val attributes = new LocalAttributeMap("error", new IllegalArgumentException("Bad account"));
        context.setCurrentEvent(new Event(this, "processing", attributes));
        val request = PasswordlessAuthenticationRequest.builder().username("casuser").build();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, passwordlessUserAccountStore.findUser(request).get());
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, displayBeforePasswordlessAuthenticationAction.execute(context).getId());
    }
}
