package org.apereo.cas;

import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.OneTimePasswordCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.policy.RequiredHandlerAuthenticationPolicyFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasMultifactorTestAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;

import lombok.val;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * High-level MFA functionality tests that leverage registered service metadata
 * ala {@link RequiredHandlerAuthenticationPolicyFactory} to drive
 * authentication policy.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@TestPropertySource(locations = {"classpath:/core.properties"},
    properties = "cas.authn.policy.requiredHandlerAuthenticationPolicyEnabled=true")
@Import(CasMultifactorTestAuthenticationEventExecutionPlanConfiguration.class)
public class MultifactorAuthenticationTests extends BaseCasWebflowMultifactorAuthenticationTests {

    private static final Service NORMAL_SERVICE = newService("https://example.com/normal/");
    private static final Service HIGH_SERVICE = newService("https://example.com/high/");
    private static final String ALICE = "alice";
    private static final String PASSWORD_31415 = "31415";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService cas;

    private static UsernamePasswordCredential newUserPassCredentials(final String user, final String pass) {
        val userpass = new UsernamePasswordCredential();
        userpass.setUsername(user);
        userpass.setPassword(pass);
        return userpass;
    }

    private static Service newService(final String id) {
        return RegisteredServiceTestUtils.getService(id);
    }

    @Test
    public void verifyAllowsAccessToNormalSecurityServiceWithPassword() {
        val ctx = processAuthenticationAttempt(NORMAL_SERVICE, newUserPassCredentials(ALICE, ALICE));
        val tgt = cas.createTicketGrantingTicket(ctx);
        assertNotNull(tgt);
        val st = cas.grantServiceTicket(tgt.getId(), NORMAL_SERVICE, ctx);
        assertNotNull(st);
    }

    @Test
    public void verifyAllowsAccessToNormalSecurityServiceWithOTP() {
        val ctx = processAuthenticationAttempt(NORMAL_SERVICE, new OneTimePasswordCredential(ALICE, PASSWORD_31415));
        val tgt = cas.createTicketGrantingTicket(ctx);
        assertNotNull(tgt);
        val st = cas.grantServiceTicket(tgt.getId(), NORMAL_SERVICE, ctx);
        assertNotNull(st);
    }

    @Test
    public void verifyDeniesAccessToHighSecurityServiceWithPassword() {
        val ctx = processAuthenticationAttempt(HIGH_SERVICE, newUserPassCredentials(ALICE, ALICE));
        this.thrown.expect(UnsatisfiedAuthenticationPolicyException.class);
        val tgt = cas.createTicketGrantingTicket(ctx);
        assertNotNull(tgt);
        cas.grantServiceTicket(tgt.getId(), HIGH_SERVICE, ctx);
    }

    @Test
    public void verifyDeniesAccessToHighSecurityServiceWithOTP() {
        val ctx = processAuthenticationAttempt(HIGH_SERVICE, new OneTimePasswordCredential(ALICE, PASSWORD_31415));
        val tgt = cas.createTicketGrantingTicket(ctx);
        assertNotNull(tgt);
        this.thrown.expect(UnsatisfiedAuthenticationPolicyException.class);
        val st = cas.grantServiceTicket(tgt.getId(), HIGH_SERVICE, ctx);
        assertNotNull(st);
    }

    @Test
    public void verifyAllowsAccessToHighSecurityServiceWithPasswordAndOTP() {
        val ctx = processAuthenticationAttempt(HIGH_SERVICE,
            newUserPassCredentials(ALICE, ALICE),
            new OneTimePasswordCredential(ALICE, PASSWORD_31415));

        val tgt = cas.createTicketGrantingTicket(ctx);
        assertNotNull(tgt);
        val st = cas.grantServiceTicket(tgt.getId(), HIGH_SERVICE, ctx);
        assertNotNull(st);
    }

    @Test
    public void verifyAllowsAccessToHighSecurityServiceWithPasswordAndOTPViaRenew() {
        val ctx2 = processAuthenticationAttempt(HIGH_SERVICE, newUserPassCredentials(ALICE, ALICE),
            new OneTimePasswordCredential(ALICE, PASSWORD_31415));

        val tgt = cas.createTicketGrantingTicket(ctx2);
        assertNotNull(tgt);

        val st = cas.grantServiceTicket(tgt.getId(), HIGH_SERVICE, ctx2);

        assertNotNull(st);
        // Confirm the authentication in the assertion is the one that satisfies security policy
        val assertion = cas.validateServiceTicket(st.getId(), HIGH_SERVICE);
        assertEquals(2, assertion.getPrimaryAuthentication().getSuccesses().size());
        assertTrue(assertion.getPrimaryAuthentication().getSuccesses().containsKey(AcceptUsersAuthenticationHandler.class.getSimpleName()));
        assertTrue(assertion.getPrimaryAuthentication().getSuccesses().containsKey(TestOneTimePasswordAuthenticationHandler.class.getSimpleName()));
        assertTrue(assertion.getPrimaryAuthentication().getAttributes().containsKey(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS));
    }

    private AuthenticationResult processAuthenticationAttempt(final Service service, final Credential... credential) throws AuthenticationException {
        return this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(service, credential);
    }
}
