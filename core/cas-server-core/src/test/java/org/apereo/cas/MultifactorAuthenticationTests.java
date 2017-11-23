package org.apereo.cas;

import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.OneTimePasswordCredential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.policy.RequiredHandlerAuthenticationPolicyFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasMultifactorTestAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.apereo.cas.validation.Assertion;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

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
        classes = {
                CasMultifactorTestAuthenticationEventExecutionPlanConfiguration.class,
                CasWebApplicationServiceFactoryConfiguration.class,
                CasDefaultServiceTicketIdGeneratorsConfiguration.class,
                CasCoreAuthenticationConfiguration.class, 
                CasCoreServicesAuthenticationConfiguration.class,
                CasCoreServicesConfiguration.class,
                CasCoreTicketCatalogConfiguration.class,
                CasCoreAuthenticationPrincipalConfiguration.class,
                CasCoreAuthenticationPolicyConfiguration.class,
                CasCoreAuthenticationMetadataConfiguration.class,
                CasCoreAuthenticationSupportConfiguration.class,
                CasCoreAuthenticationHandlersConfiguration.class,
                CasCoreHttpConfiguration.class,
                AopAutoConfiguration.class,
                CasCoreUtilConfiguration.class,
                CasCoreConfiguration.class,
                CasRegisteredServicesTestConfiguration.class,
                CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
                CasCoreLogoutConfiguration.class,
                RefreshAutoConfiguration.class,
                CasCoreTicketsConfiguration.class,
                CasPersonDirectoryTestConfiguration.class,
                CasCoreTicketIdGeneratorsConfiguration.class,
                CasCoreValidationConfiguration.class,
                CasCoreWebConfiguration.class})
@TestPropertySource(locations = {"classpath:/core.properties"}, properties = "cas.authn.policy.requiredHandlerAuthenticationPolicyEnabled=true")
public class MultifactorAuthenticationTests {

    private static final Service NORMAL_SERVICE = newService("https://example.com/normal/");
    private static final Service HIGH_SERVICE = newService("https://example.com/high/");
    private static final String ALICE = "alice";
    private static final String PASSWORD_31415 = "31415";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired(required = false)
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService cas;

    @Test
    public void verifyAllowsAccessToNormalSecurityServiceWithPassword() {
        final AuthenticationResult ctx = processAuthenticationAttempt(NORMAL_SERVICE, newUserPassCredentials(ALICE, ALICE));
        final TicketGrantingTicket tgt = cas.createTicketGrantingTicket(ctx);
        assertNotNull(tgt);
        final ServiceTicket st = cas.grantServiceTicket(tgt.getId(), NORMAL_SERVICE, ctx);
        assertNotNull(st);
    }

    @Test
    public void verifyAllowsAccessToNormalSecurityServiceWithOTP() {
        final AuthenticationResult ctx = processAuthenticationAttempt(NORMAL_SERVICE, new OneTimePasswordCredential(ALICE, PASSWORD_31415));
        final TicketGrantingTicket tgt = cas.createTicketGrantingTicket(ctx);
        assertNotNull(tgt);
        final ServiceTicket st = cas.grantServiceTicket(tgt.getId(), NORMAL_SERVICE, ctx);
        assertNotNull(st);
    }

    @Test
    public void verifyDeniesAccessToHighSecurityServiceWithPassword() {
        final AuthenticationResult ctx = processAuthenticationAttempt(HIGH_SERVICE, newUserPassCredentials(ALICE, ALICE));
        this.thrown.expect(UnsatisfiedAuthenticationPolicyException.class);
        final TicketGrantingTicket tgt = cas.createTicketGrantingTicket(ctx);
        assertNotNull(tgt);
        cas.grantServiceTicket(tgt.getId(), HIGH_SERVICE, ctx);
    }

    @Test
    public void verifyDeniesAccessToHighSecurityServiceWithOTP() {
        final AuthenticationResult ctx = processAuthenticationAttempt(HIGH_SERVICE, new OneTimePasswordCredential(ALICE, PASSWORD_31415));
        final TicketGrantingTicket tgt = cas.createTicketGrantingTicket(ctx);
        assertNotNull(tgt);
        this.thrown.expect(UnsatisfiedAuthenticationPolicyException.class);
        final ServiceTicket st = cas.grantServiceTicket(tgt.getId(), HIGH_SERVICE, ctx);
        assertNotNull(st);
    }

    @Test
    public void verifyAllowsAccessToHighSecurityServiceWithPasswordAndOTP() {
        final AuthenticationResult ctx = processAuthenticationAttempt(HIGH_SERVICE,
                newUserPassCredentials(ALICE, ALICE),
                new OneTimePasswordCredential(ALICE, PASSWORD_31415));

        final TicketGrantingTicket tgt = cas.createTicketGrantingTicket(ctx);
        assertNotNull(tgt);
        final ServiceTicket st = cas.grantServiceTicket(tgt.getId(), HIGH_SERVICE, ctx);
        assertNotNull(st);
    }

    @Test
    public void verifyAllowsAccessToHighSecurityServiceWithPasswordAndOTPViaRenew() {
        // Note the original credential used to start SSO session does not satisfy security policy
        final AuthenticationResult ctx2 = processAuthenticationAttempt(HIGH_SERVICE, newUserPassCredentials(ALICE, ALICE),
                new OneTimePasswordCredential(ALICE, PASSWORD_31415));

        final TicketGrantingTicket tgt = cas.createTicketGrantingTicket(ctx2);
        assertNotNull(tgt);

        final ServiceTicket st = cas.grantServiceTicket(tgt.getId(), HIGH_SERVICE, ctx2);

        assertNotNull(st);
        // Confirm the authentication in the assertion is the one that satisfies security policy
        final Assertion assertion = cas.validateServiceTicket(st.getId(), HIGH_SERVICE);
        assertEquals(2, assertion.getPrimaryAuthentication().getSuccesses().size());
        assertTrue(assertion.getPrimaryAuthentication().getSuccesses().containsKey(AcceptUsersAuthenticationHandler.class.getSimpleName()));
        assertTrue(assertion.getPrimaryAuthentication().getSuccesses().containsKey(TestOneTimePasswordAuthenticationHandler.class.getSimpleName()));
        assertTrue(assertion.getPrimaryAuthentication().getAttributes().containsKey(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS));
    }

    private static UsernamePasswordCredential newUserPassCredentials(final String user, final String pass) {
        final UsernamePasswordCredential userpass = new UsernamePasswordCredential();
        userpass.setUsername(user);
        userpass.setPassword(pass);
        return userpass;
    }

    private static Service newService(final String id) {
        return CoreAuthenticationTestUtils.getService(id);
    }

    private AuthenticationResult processAuthenticationAttempt(final Service service, final Credential... credential) throws AuthenticationException {
        return this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(service, credential);
    }

    
}
