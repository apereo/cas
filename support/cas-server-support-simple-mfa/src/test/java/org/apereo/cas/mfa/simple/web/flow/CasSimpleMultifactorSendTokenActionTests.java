package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.authentication.MultifactorAuthenticationFailedException;
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
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.LocalAttributeMap;
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
    class MultipleContactInfoTests extends BaseCasSimpleMultifactorSendTokenActionTests {
        @Test
        void verifyOperation() throws Throwable {
            val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap(
                    "mail", List.of("cas@example.org", "user@example.com"),
                    "phone", List.of("6024351243", "5034351243")
                ));
            val requestContext = buildRequestContextFor(principal);
            var event = mfaSimpleMultifactorSendTokenAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SELECT, event.getId());
            assertTrue(requestContext.getFlowScope().contains(CasSimpleMultifactorSendTokenAction.FLOW_SCOPE_ATTR_EMAIL_RECIPIENTS, Map.class));
            assertTrue(requestContext.getFlowScope().contains(CasSimpleMultifactorSendTokenAction.FLOW_SCOPE_ATTR_SMS_RECIPIENTS, Map.class));
            val emailRecipients = requestContext.getFlowScope().get(CasSimpleMultifactorSendTokenAction.FLOW_SCOPE_ATTR_EMAIL_RECIPIENTS, Map.class);
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
            assertThrows(MultifactorAuthenticationFailedException.class, () -> authenticationHandler.authenticate(token, mock(Service.class)));
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=25000",

        "cas.authn.mfa.simple.mail.from=admin@example.org",
        "cas.authn.mfa.simple.mail.subject=CAS Token",
        "cas.authn.mfa.simple.mail.text=CAS Token is ${token}",
        "cas.authn.mfa.simple.mail.accepted-email-pattern=.+@example.org"
    })
    class EmailRegistrationTests extends BaseCasSimpleMultifactorSendTokenActionTests {
        
        @Test
        void verifyEmailRegistration() throws Throwable {
            val requestContext = buildRequestContextFor("casuser", null);
            val event = mfaSimpleMultifactorSendTokenAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_REGISTER, event.getId());
            val attributes = event.getAttributes();
            assertTrue(attributes.contains("principal"));
            assertTrue(attributes.contains("authentication"));
            assertTrue(attributes.contains(CasSimpleMultifactorSendTokenAction.EVENT_ATTR_ALLOW_REGISTER_EMAIL));
        }

        @Test
        void verifyResumeEmailRegistration() throws Throwable {
            val requestContext = buildRequestContextFor("casuser", null);
            requestContext.setCurrentEvent(new EventFactorySupport().event(this,
                CasWebflowConstants.TRANSITION_ID_RESUME, new LocalAttributeMap<>(
                    Map.of(CasSimpleMultifactorVerifyEmailAction.TOKEN_PROPERTY_EMAIL_TO_REGISTER, "casuser@example.org"))));
            val event = mfaSimpleMultifactorSendTokenAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
            val attributes = event.getAttributes();
            assertTrue(attributes.contains("token"));
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
