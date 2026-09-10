package org.apereo.cas.mfa.simple.web.flow;

import module java.base;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasSimpleMultifactorVerifyEmailActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@EnabledIfListeningOnPort(port = 25000)
@Tag("Mail")
class CasSimpleMultifactorVerifyEmailActionTests {
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
        void verifyMissingEmail() throws Throwable {
            val requestContext = buildRequestContextFor(RegisteredServiceTestUtils.getPrincipal("casuser"));
            val event = mfaSimpleMultifactorVerifyEmailAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        }

        @Test
        void verifyInvalidEmail() throws Throwable {
            val requestContext = buildRequestContextFor(RegisteredServiceTestUtils.getPrincipal("casuser"));
            requestContext.setParameter("email", "$$$");
            val event = mfaSimpleMultifactorVerifyEmailAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        }

        @Test
        void verifyUnknownEmail() throws Throwable {
            val requestContext = buildRequestContextFor(RegisteredServiceTestUtils.getPrincipal("casuser"));
            requestContext.setParameter("email", "user@company.org");
            val event = mfaSimpleMultifactorVerifyEmailAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        }

        @Test
        void verifyValidEmail() throws Throwable {
            val requestContext = buildRequestContextFor(RegisteredServiceTestUtils.getPrincipal("casuser"));
            requestContext.setParameter("email", "cas@example.org");
            val event = mfaSimpleMultifactorVerifyEmailAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
            val token = event.getAttributes().get("result");
            assertNotNull(token);
        }

        @Test
        void verifyEachRegistrationAttemptIssuesFreshToken() throws Throwable {
            val requestContext = buildRequestContextFor(RegisteredServiceTestUtils.getPrincipal("casuser"));
            requestContext.setParameter("email", "cas@example.org");
            var event = mfaSimpleMultifactorVerifyEmailAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
            val firstToken = (CasSimpleMultifactorAuthenticationTicket) event.getAttributes().get("result");

            requestContext.setParameter("email", "cas-alt@example.org");
            event = mfaSimpleMultifactorVerifyEmailAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
            val secondToken = (CasSimpleMultifactorAuthenticationTicket) event.getAttributes().get("result");

            assertNotEquals(firstToken.getId(), secondToken.getId());
            assertEquals("cas@example.org",
                firstToken.getProperty(CasSimpleMultifactorVerifyEmailAction.TOKEN_PROPERTY_EMAIL_TO_REGISTER, String.class));
            assertEquals("cas-alt@example.org",
                secondToken.getProperty(CasSimpleMultifactorVerifyEmailAction.TOKEN_PROPERTY_EMAIL_TO_REGISTER, String.class));
        }
    }
    
}
