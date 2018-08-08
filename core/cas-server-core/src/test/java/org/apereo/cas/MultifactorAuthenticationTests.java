package org.apereo.cas;

import org.apereo.cas.authentication.AcceptUsersAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
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
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;

/**
 * High-level MFA functionality tests that leverage registered service metadata
 * ala {@link RequiredHandlerAuthenticationPolicyFactory} to drive
 * authentication policy.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@SpringBootTest(classes = {
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
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    private static final Service NORMAL_SERVICE = newService("https://example.com/normal/");
    private static final Service HIGH_SERVICE = newService("https://example.com/high/");
    private static final String ALICE = "alice";
    private static final String PASSWORD_31415 = "31415";

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

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
