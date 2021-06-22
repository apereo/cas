package org.apereo.cas.mfa.simple.web.flow;

import org.apereo.cas.mfa.simple.BaseCasSimpleMultifactorAuthenticationTests;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCredential;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import javax.security.auth.login.FailedLoginException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasSimpleMultifactorSendTokenActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnabledIfPortOpen(port = 25000)
@Tag("Mail")
public class CasSimpleMultifactorSendTokenActionTests {
    @SuppressWarnings("ClassCanBeStatic")
    @Nested
    @TestPropertySource(properties = {
        "spring.mail.host=localhost",
        "spring.mail.port=25000",

        "cas.authn.mfa.simple.mail.from=admin@example.org",
        "cas.authn.mfa.simple.mail.subject=CAS Token",
        "cas.authn.mfa.simple.mail.text=CAS Token is %s",

        "cas.authn.mfa.simple.sms.from=347746512"
    })
    @Import(BaseCasSimpleMultifactorAuthenticationTests.CasSimpleMultifactorTestConfiguration.class)
    public class DefaultCasSimpleMultifactorSendTokenActionTests extends BaseCasSimpleMultifactorSendTokenActionTests {
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
    }

    @SuppressWarnings("ClassCanBeStatic")
    @Nested
    public class NoCommunicationStrategyTests extends BaseCasSimpleMultifactorSendTokenActionTests {
        @Test
        public void verifyOperation() throws Exception {
            val context = buildRequestContextFor("casuser");
            val event = mfaSimpleMultifactorSendTokenAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        }
    }
}
