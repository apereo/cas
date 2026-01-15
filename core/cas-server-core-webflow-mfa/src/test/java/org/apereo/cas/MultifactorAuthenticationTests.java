package org.apereo.cas;

import module java.base;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.OneTimePasswordCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasMultifactorAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * High-level MFA functionality tests that leverage registered service metadata
 * to drive authentication policy.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@TestPropertySource(properties = {
    "cas.authn.policy.required-handler-authentication-policy-enabled=true",
    "cas.authn.policy.any.try-all=true",
    "cas.ticket.st.time-to-kill-in-seconds=30"
})
@Import(CasMultifactorAuthenticationEventExecutionPlanTestConfiguration.class)
@Tag("MFA")
@ExtendWith(CasTestExtension.class)
class MultifactorAuthenticationTests extends BaseCasWebflowMultifactorAuthenticationTests {

    private static final Service NORMAL_SERVICE = newService("https://example.com/normal/");

    private static final Service HIGH_SERVICE = newService("https://example.com/high/");

    private static final String ALICE = "alice";

    private static final String PASSWORD_31415 = "31415";

    @Autowired
    @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier(CentralAuthenticationService.BEAN_NAME)
    private CentralAuthenticationService cas;

    private static UsernamePasswordCredential newUserPassCredentials(final String user,
                                                                     final String pass) {
        val credential = new UsernamePasswordCredential();
        credential.setUsername(user);
        credential.assignPassword(pass);
        return credential;
    }

    private static Service newService(final String id) {
        return RegisteredServiceTestUtils.getService(id);
    }

    @Test
    void verifyAllowsAccessToNormalSecurityServiceWithPassword() throws Throwable {
        val ctx = processAuthenticationAttempt(NORMAL_SERVICE, newUserPassCredentials(ALICE, ALICE));
        val tgt = cas.createTicketGrantingTicket(ctx);
        assertNotNull(tgt);
        val st = cas.grantServiceTicket(tgt.getId(), NORMAL_SERVICE, ctx);
        assertNotNull(st);
    }

    @Test
    void verifyAllowsAccessToNormalSecurityServiceWithOTP() throws Throwable {
        val ctx = processAuthenticationAttempt(NORMAL_SERVICE, new OneTimePasswordCredential(ALICE, PASSWORD_31415));
        val tgt = cas.createTicketGrantingTicket(ctx);
        assertNotNull(tgt);
        val st = cas.grantServiceTicket(tgt.getId(), NORMAL_SERVICE, ctx);
        assertNotNull(st);
    }

    @Test
    void verifyDeniesAccessToHighSecurityServiceWithPassword() throws Throwable {
        val ctx = processAuthenticationAttempt(HIGH_SERVICE, newUserPassCredentials(ALICE, ALICE));
        val tgt = cas.createTicketGrantingTicket(ctx);
        assertNotNull(tgt);
        assertThrows(UnsatisfiedAuthenticationPolicyException.class, () -> cas.grantServiceTicket(tgt.getId(), HIGH_SERVICE, ctx));
    }

    @Test
    void verifyDeniesAccessToHighSecurityServiceWithOTP() throws Throwable {
        val ctx = processAuthenticationAttempt(HIGH_SERVICE, new OneTimePasswordCredential(ALICE, PASSWORD_31415));
        val tgt = cas.createTicketGrantingTicket(ctx);
        assertNotNull(tgt);
        assertThrows(UnsatisfiedAuthenticationPolicyException.class, () -> cas.grantServiceTicket(tgt.getId(), HIGH_SERVICE, ctx));
    }

    @Test
    void verifyAllowsAccessToHighSecurityServiceWithPasswordAndOTP() throws Throwable {
        val ctx = processAuthenticationAttempt(HIGH_SERVICE,
            newUserPassCredentials(ALICE, ALICE),
            new OneTimePasswordCredential(ALICE, PASSWORD_31415));

        val tgt = cas.createTicketGrantingTicket(ctx);
        assertNotNull(tgt);
        val st = cas.grantServiceTicket(tgt.getId(), HIGH_SERVICE, ctx);
        assertNotNull(st);
    }

    @Test
    void verifyAllowsAccessToHighSecurityServiceWithPasswordAndOTPViaRenew() throws Throwable {
        val ctx2 = processAuthenticationAttempt(HIGH_SERVICE, newUserPassCredentials(ALICE, ALICE),
            new OneTimePasswordCredential(ALICE, PASSWORD_31415));

        val tgt = cas.createTicketGrantingTicket(ctx2);
        assertNotNull(tgt);

        val st = cas.grantServiceTicket(tgt.getId(), HIGH_SERVICE, ctx2);

        assertNotNull(st);
        /*
         * Confirm the authentication in the assertion
         * is the one that satisfies security policy
         */
        val assertion = cas.validateServiceTicket(st.getId(), HIGH_SERVICE);
        val authn = assertion.getPrimaryAuthentication();
        assertEquals(2, authn.getSuccesses().size());
        assertTrue(authn.getSuccesses().containsKey(AcceptUsersAuthenticationHandler.class.getSimpleName()));
        assertTrue(authn.getSuccesses().containsKey(TestOneTimePasswordAuthenticationHandler.class.getSimpleName()));
        assertTrue(authn.containsAttribute(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS));
    }

    private AuthenticationResult processAuthenticationAttempt(final Service service, final Credential... credential) throws Throwable {
        return authenticationSystemSupport.finalizeAuthenticationTransaction(service, credential);
    }
}
