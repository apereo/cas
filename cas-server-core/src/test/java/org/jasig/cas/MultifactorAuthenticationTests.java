package org.jasig.cas;

import org.jasig.cas.authentication.AuthenticationResult;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.AuthenticationSystemSupport;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.DefaultAuthenticationSystemSupport;
import org.jasig.cas.authentication.OneTimePasswordCredential;
import org.jasig.cas.authentication.SuccessfulHandlerMetaDataPopulator;
import org.jasig.cas.util.AuthTestUtils;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.jasig.cas.validation.Assertion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.validation.constraints.NotNull;

import static org.junit.Assert.*;

/**
 * High-level MFA functionality tests that leverage registered service metadata
 * ala {@link org.jasig.cas.authentication.RequiredHandlerAuthenticationPolicyFactory} to drive
 * authentication policy.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/mfa-test-context.xml"})
public class MultifactorAuthenticationTests {
    private static final Service NORMAL_SERVICE = newService("https://example.com/normal/");
    private static final Service HIGH_SERVICE = newService("https://example.com/high/");

    @NotNull
    @Autowired(required=false)
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport = new DefaultAuthenticationSystemSupport();

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService cas;

    @Test
    public void verifyAllowsAccessToNormalSecurityServiceWithPassword() throws Exception {

        final AuthenticationResult ctx = processAuthenticationAttempt(NORMAL_SERVICE, newUserPassCredentials("alice", "alice"));

        final TicketGrantingTicket tgt = cas.createTicketGrantingTicket(ctx);
        assertNotNull(tgt);
        final ServiceTicket st = cas.grantServiceTicket(tgt.getId(), NORMAL_SERVICE, ctx);
        assertNotNull(st);
    }

    @Test
    public void verifyAllowsAccessToNormalSecurityServiceWithOTP() throws Exception {
        final AuthenticationResult ctx = processAuthenticationAttempt(NORMAL_SERVICE, new OneTimePasswordCredential("alice", "31415"));

        final TicketGrantingTicket tgt = cas.createTicketGrantingTicket(ctx);
        assertNotNull(tgt);
        final ServiceTicket st = cas.grantServiceTicket(tgt.getId(), NORMAL_SERVICE, ctx);
        assertNotNull(st);
    }

    @Test(expected = UnsatisfiedAuthenticationPolicyException.class)
    public void verifyDeniesAccessToHighSecurityServiceWithPassword() throws Exception {
        final AuthenticationResult ctx = processAuthenticationAttempt(HIGH_SERVICE, newUserPassCredentials("alice", "alice"));

        final TicketGrantingTicket tgt = cas.createTicketGrantingTicket(ctx);
        assertNotNull(tgt);
        cas.grantServiceTicket(tgt.getId(), HIGH_SERVICE, ctx);
    }

    @Test(expected = UnsatisfiedAuthenticationPolicyException.class)
    public void verifyDeniesAccessToHighSecurityServiceWithOTP() throws Exception {
        final AuthenticationResult ctx =  processAuthenticationAttempt(HIGH_SERVICE, new OneTimePasswordCredential("alice", "31415"));

        final TicketGrantingTicket tgt = cas.createTicketGrantingTicket(ctx);
        assertNotNull(tgt);
        final ServiceTicket st = cas.grantServiceTicket(tgt.getId(), HIGH_SERVICE, ctx);
        assertNotNull(st);
    }

    @Test
    public void verifyAllowsAccessToHighSecurityServiceWithPasswordAndOTP() throws Exception {
        final AuthenticationResult ctx = processAuthenticationAttempt(HIGH_SERVICE,
                newUserPassCredentials("alice", "alice"),
                new OneTimePasswordCredential("alice", "31415"));

        final TicketGrantingTicket tgt = cas.createTicketGrantingTicket(ctx);
        assertNotNull(tgt);
        final ServiceTicket st = cas.grantServiceTicket(tgt.getId(), HIGH_SERVICE, ctx);
        assertNotNull(st);
    }

    @Test
    public void verifyAllowsAccessToHighSecurityServiceWithPasswordAndOTPViaRenew() throws Exception {
        // Note the original credential used to start SSO session does not satisfy security policy

        final AuthenticationResult ctx = processAuthenticationAttempt(HIGH_SERVICE, newUserPassCredentials("alice", "alice"));
        final TicketGrantingTicket tgt = cas.createTicketGrantingTicket(ctx);
        assertNotNull(tgt);
        final AuthenticationResult ctx2 = processAuthenticationAttempt(HIGH_SERVICE, newUserPassCredentials("alice", "alice"),
                new OneTimePasswordCredential("alice", "31415"));

        final ServiceTicket st = cas.grantServiceTicket(
                tgt.getId(),
                HIGH_SERVICE,
                ctx2);

        assertNotNull(st);
        // Confirm the authentication in the assertion is the one that satisfies security policy
        final Assertion assertion = cas.validateServiceTicket(st.getId(), HIGH_SERVICE);
        assertEquals(2, assertion.getPrimaryAuthentication().getSuccesses().size());
        assertTrue(assertion.getPrimaryAuthentication().getSuccesses().containsKey("passwordHandler"));
        assertTrue(assertion.getPrimaryAuthentication().getSuccesses().containsKey("oneTimePasswordHandler"));
        assertTrue(assertion.getPrimaryAuthentication().getAttributes().containsKey(
                SuccessfulHandlerMetaDataPopulator.SUCCESSFUL_AUTHENTICATION_HANDLERS));
    }

    private static UsernamePasswordCredential newUserPassCredentials(final String user, final String pass) {
        final UsernamePasswordCredential userpass = new UsernamePasswordCredential();
        userpass.setUsername(user);
        userpass.setPassword(pass);
        return userpass;
    }

    private static Service newService(final String id) {
        return AuthTestUtils.getService(id);
    }

    private AuthenticationResult processAuthenticationAttempt(final Service service, final Credential... credential) throws
            AuthenticationException {

        return this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(service, credential);
    }
}
