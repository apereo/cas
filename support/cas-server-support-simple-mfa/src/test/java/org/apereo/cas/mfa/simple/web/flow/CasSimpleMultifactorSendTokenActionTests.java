package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.mfa.simple.BaseCasSimpleMultifactorAuthenticationTests;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCredential;
import org.apereo.cas.notifications.call.PhoneCallOperator;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import javax.security.auth.login.FailedLoginException;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasSimpleMultifactorSendTokenActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnabledIfListeningOnPort(port = 25000)
@Tag("Mail")
class CasSimpleMultifactorSendTokenActionTests {
    @Nested
    @TestPropertySource(properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=25000",

        "cas.authn.mfa.simple.sms.from=347746512",
        "cas.authn.mfa.simple.sms.text=Your token: ${token}",

        "cas.authn.mfa.simple.mail.from=admin@example.org",
        "cas.authn.mfa.simple.mail.subject=CAS Token",
        "cas.authn.mfa.simple.mail.text=CAS Token is ${token}"
    })
    @Import(BaseCasSimpleMultifactorAuthenticationTests.CasSimpleMultifactorTestConfiguration.class)
    class MultipleEmailsTests extends BaseCasSimpleMultifactorSendTokenActionTests {
        @Test
        void verifyOperation() throws Throwable {
            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("mail", List.of("cas@example.org", "user@example.com")));
            val requestContext = buildRequestContextFor(principal);
            var event = mfaSimpleMultifactorSendTokenAction.execute(requestContext);
            assertEquals("selectEmails", event.getId());
            assertTrue(requestContext.getFlowScope().contains("emailRecipients", Map.class));
            val emailRecipients = requestContext.getFlowScope().get("emailRecipients", Map.class);
            emailRecipients.keySet().forEach(key -> requestContext.setParameter(key.toString(), "nothing"));
            event = mfaSimpleMultifactorSendTokenAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=25000",

        "cas.authn.mfa.simple.mail.from=admin@example.org",
        "cas.authn.mfa.simple.mail.subject=CAS Token",
        "cas.authn.mfa.simple.mail.text=CAS Token is ${token}",

        "cas.authn.mfa.simple.sms.from=347746512"
    })
    @Import(PhoneCallTests.PhoneCallOperatorTestConfiguration.class)
    class PhoneCallTests extends BaseCasSimpleMultifactorSendTokenActionTests {

        @TestConfiguration(value = "PhoneCallOperatorTestConfiguration", proxyBeanMethods = false)
        public static class PhoneCallOperatorTestConfiguration {
            @Bean
            public PhoneCallOperator phoneCallOperator() {
                return new PhoneCallOperator() {
                    @Override
                    public boolean call(final String from, final String to, final String message) {
                        return true;
                    }
                };
            }
        }

        @Test
        void verifyOperation() throws Throwable {
            val context = buildRequestContextFor("casuser");
            val event = mfaSimpleMultifactorSendTokenAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=25000",

        "cas.authn.mfa.simple.mail.from=admin@example.org",
        "cas.authn.mfa.simple.mail.subject=CAS Token",
        "cas.authn.mfa.simple.mail.text=CAS Token is ${token}",

        "cas.authn.mfa.simple.sms.from=347746512"
    })
    @Import(BaseCasSimpleMultifactorAuthenticationTests.CasSimpleMultifactorTestConfiguration.class)
    class DefaultTests extends BaseCasSimpleMultifactorSendTokenActionTests {
        @Test
        void verifyOperation() throws Throwable {
            val theToken = createToken("casuser").getKey();
            assertNotNull(ticketRegistry.getTicket(theToken));
            val token = new CasSimpleMultifactorTokenCredential(theToken);
            val result = authenticationHandler.authenticate(token, mock(Service.class));
            assertNotNull(result);
            assertNull(ticketRegistry.getTicket(theToken));
        }

        @Test
        void verifyReusingExistingTokens() throws Throwable {
            val pair = createToken("casuser");

            val theToken = pair.getKey();
            assertNotNull(ticketRegistry.getTicket(theToken));

            val event = mfaSimpleMultifactorSendTokenAction.execute(pair.getValue());
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());

            val token = new CasSimpleMultifactorTokenCredential(theToken);
            val result = authenticationHandler.authenticate(token, mock(Service.class));
            assertNotNull(result);
            assertNull(ticketRegistry.getTicket(theToken));
        }

        @Test
        void verifyFailsForUser() throws Throwable {
            val theToken1 = createToken("casuser1");
            assertNotNull(theToken1);

            val theToken2 = createToken("casuser2");
            assertNotNull(theToken2);
            
            val token = new CasSimpleMultifactorTokenCredential(theToken1.getKey());
            ticketRegistry.deleteTicket(theToken1.getKey());

            assertThrows(FailedLoginException.class, () -> authenticationHandler.authenticate(token, mock(Service.class)));
        }
    }

    @Nested
    class NoCommunicationStrategyTests extends BaseCasSimpleMultifactorSendTokenActionTests {
        @Test
        void verifyOperation() throws Throwable {
            val context = buildRequestContextFor("casuser");
            val event = mfaSimpleMultifactorSendTokenAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        }
    }
}
