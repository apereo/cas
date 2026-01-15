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
 * This is {@link CasSimpleMultifactorUpdateEmailActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@EnabledIfListeningOnPort(port = 25000)
@Tag("Mail")
class CasSimpleMultifactorUpdateEmailActionTests {
    @Nested
    @TestPropertySource(properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=25000",

        "cas.authn.mfa.simple.mail.from=admin@example.org",
        "cas.authn.mfa.simple.mail.subject=CAS Token",
        "cas.authn.mfa.simple.mail.text=CAS Token is ${token}",
        "cas.authn.mfa.simple.mail.accepted-email-pattern=.+@example.org"
    })
    class DefaultTests extends BaseCasSimpleMultifactorSendTokenActionTests {
        @Test
        void verifyUpdate() throws Throwable {
            val requestContext = buildRequestContextFor(RegisteredServiceTestUtils.getPrincipal("casuser"));
            requestContext.setParameter("email", "cas@example.org");
            var event = mfaSimpleMultifactorVerifyEmailAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
            val token = (CasSimpleMultifactorAuthenticationTicket) event.getAttributes().get("result");
            requestContext.setParameter("token", token.getId());
            event = mfaSimpleMultifactorUpdateEmailAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_RESUME, event.getId());
        }

        @Test
        void verifyInvalidToken() throws Throwable {
            val requestContext = buildRequestContextFor(RegisteredServiceTestUtils.getPrincipal("casuser"));
            requestContext.setParameter("email", "cas@example.org");
            var event = mfaSimpleMultifactorVerifyEmailAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
            val token = (CasSimpleMultifactorAuthenticationTicket) event.getAttributes().get("result");
            requestContext.setParameter("token", token.getId());
            ticketRegistry.deleteTicket(token.getId());
            event = mfaSimpleMultifactorUpdateEmailAction.execute(requestContext);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        }
    }
}
