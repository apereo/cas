package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.BaseCasSimpleMultifactorAuthenticationTests;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCredential;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockSmsSender;
import org.apereo.cas.util.io.SmsSender;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import javax.security.auth.login.FailedLoginException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasSimpleSendTokenActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = BaseCasSimpleMultifactorAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=25000",
        "cas.authn.mfa.simple.mail.from=admin@example.org",
        "cas.authn.mfa.simple.mail.subject=CAS Token",
        "cas.authn.mfa.simple.mail.text=CAS Token is %s",
        "cas.authn.mfa.simple.sms.from=347746512"
    })
@EnabledIfPortOpen(port = 25000)
@Tag("Mail")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasSimpleSendTokenActionTests {
    @Autowired
    @Qualifier("mfaSimpleMultifactorSendTokenAction")
    private Action mfaSimpleMultifactorSendTokenAction;

    @Autowired
    @Qualifier("casSimpleMultifactorAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Test
    public void verifyOperation() throws Exception {
        val theToken = createToken("casuser").getKey();
        assertNotNull(this.ticketRegistry.getTicket(theToken));
        val token = new CasSimpleMultifactorTokenCredential(theToken);
        val result = authenticationHandler.authenticate(token);
        assertNotNull(result);
        assertNull(this.ticketRegistry.getTicket(theToken));
    }

    @Test
    public void verifyFailsForUser() throws Exception {
        val theToken1 = createToken("casuser1");
        assertNotNull(theToken1);
        
        val theToken2 = createToken("casuser2");
        assertNotNull(theToken2);
        val token = new CasSimpleMultifactorTokenCredential(theToken1.getKey());
        assertThrows(FailedLoginException.class, () -> authenticationHandler.authenticate(token));

    }

    protected Pair<String, RequestContext> createToken(final String user) throws Exception {
        val context = buildRequestContextFor(user);
        val event = mfaSimpleMultifactorSendTokenAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        return Pair.of(event.getAttributes().getString("token"), context);
    }

    private static MockRequestContext buildRequestContextFor(final String user) {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        WebUtils.putServiceIntoFlashScope(context, RegisteredServiceTestUtils.getService());

        val principal = RegisteredServiceTestUtils.getPrincipal(user,
            CollectionUtils.wrap("phone", List.of("123456789"), "mail", List.of("cas@example.org")));
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(principal), context);
        return context;
    }

    @TestConfiguration
    @Lazy(false)
    public static class CasSimpleMultifactorTestConfiguration {
        @Bean
        public SmsSender smsSender() {
            return new MockSmsSender();
        }
    }
}
