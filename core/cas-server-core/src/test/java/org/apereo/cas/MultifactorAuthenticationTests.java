package org.apereo.cas;

import com.google.common.collect.ImmutableMap;
import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.DefaultAuthenticationSystemSupport;
import org.apereo.cas.authentication.OneTimePasswordCredential;
import org.apereo.cas.authentication.RequiredHandlerAuthenticationPolicyFactory;
import org.apereo.cas.authentication.TestUtils;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAttributeRepositoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * High-level MFA functionality tests that leverage registered service metadata
 * ala {@link RequiredHandlerAuthenticationPolicyFactory} to drive
 * authentication policy.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {CasCoreAuthenticationConfiguration.class,
                CasCoreServicesConfiguration.class,
                CasCoreUtilConfiguration.class,
                CasPersonDirectoryAttributeRepositoryConfiguration.class,
                CasCoreConfiguration.class,
                CasCoreLogoutConfiguration.class,
                RefreshAutoConfiguration.class,
                CasCoreTicketsConfiguration.class,
                CasCoreValidationConfiguration.class})
@ContextConfiguration(locations = {"/mfa-test-context.xml"})
@TestPropertySource(locations = {"classpath:/core.properties"}, properties = "cas.authn.policy.requiredHandlerAuthenticationPolicyEnabled=true")
public class MultifactorAuthenticationTests {
    private static final Service NORMAL_SERVICE = newService("https://example.com/normal/");
    private static final Service HIGH_SERVICE = newService("https://example.com/high/");

    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map authenticationHandlersResolvers;

    @Autowired(required = false)
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport = new DefaultAuthenticationSystemSupport();

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService cas;

    @PostConstruct
    public void init() {
        authenticationHandlersResolvers.put(new AcceptUsersAuthenticationHandler(
                ImmutableMap.of("alice", "alice", "bob", "bob", "mallory", "mallory")
        ), null);
        authenticationHandlersResolvers.put(new TestOneTimePasswordAuthenticationHandler(
                ImmutableMap.of("alice", "31415", "bob", "62831", "mallory", "14142")
        ), null);
    }

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
        final AuthenticationResult ctx = processAuthenticationAttempt(HIGH_SERVICE, new OneTimePasswordCredential("alice", "31415"));

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
        final AuthenticationResult ctx2 = processAuthenticationAttempt(HIGH_SERVICE, newUserPassCredentials("alice", "alice"),
                new OneTimePasswordCredential("alice", "31415"));

        final TicketGrantingTicket tgt = cas.createTicketGrantingTicket(ctx2);
        assertNotNull(tgt);

        final ServiceTicket st = cas.grantServiceTicket(
                tgt.getId(),
                HIGH_SERVICE,
                ctx2);

        assertNotNull(st);
        // Confirm the authentication in the assertion is the one that satisfies security policy
        final Assertion assertion = cas.validateServiceTicket(st.getId(), HIGH_SERVICE);
        assertEquals(2, assertion.getPrimaryAuthentication().getSuccesses().size());
        assertTrue(assertion.getPrimaryAuthentication()
                .getSuccesses().containsKey(AcceptUsersAuthenticationHandler.class.getSimpleName()));
        assertTrue(assertion.getPrimaryAuthentication()
                .getSuccesses().containsKey(TestOneTimePasswordAuthenticationHandler.class.getSimpleName()));
        assertTrue(assertion.getPrimaryAuthentication().getAttributes().containsKey(
                AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS));
    }

    private static UsernamePasswordCredential newUserPassCredentials(final String user, final String pass) {
        final UsernamePasswordCredential userpass = new UsernamePasswordCredential();
        userpass.setUsername(user);
        userpass.setPassword(pass);
        return userpass;
    }

    private static Service newService(final String id) {
        return TestUtils.getService(id);
    }

    private AuthenticationResult processAuthenticationAttempt(final Service service, final Credential... credential) throws
            AuthenticationException {

        return this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(service, credential);
    }
}
