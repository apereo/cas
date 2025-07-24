package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ServiceTicketGeneratorAuthority;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AccountProfilePreparePasswordManagementActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("WebflowAccountActions")
@TestPropertySource(properties = {
    "cas.authn.pm.groovy.location=classpath:PasswordManagementService.groovy",
    "cas.authn.pm.core.enabled=true",
    "cas.authn.pm.forgot-username.enabled=false",
    "cas.authn.pm.reset.security-questions-enabled=true",
    "CasFeatureModule.AccountManagement.enabled=true"
})
@ImportAutoConfiguration(CasCoreWebflowAutoConfiguration.class)
class AccountProfilePreparePasswordManagementActionTests extends BasePasswordManagementActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_PREPARE_ACCOUNT_PASSWORD_MANAGEMENT)
    private Action prepareAccountProfilePasswordMgmtAction;

    @Autowired
    @Qualifier("accountProfileServiceTicketGeneratorAuthority")
    private ServiceTicketGeneratorAuthority accountProfileServiceTicketGeneratorAuthority;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val tgt = new MockTicketGrantingTicket("casuser");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        WebUtils.putTicketGrantingTicket(context, tgt);
        ticketRegistry.addTicket(tgt);
        val result = prepareAccountProfilePasswordMgmtAction.execute(context);
        assertNull(result);
        assertTrue(WebUtils.isPasswordManagementEnabled(context));
        assertFalse(WebUtils.isForgotUsernameEnabled(context));
        assertNotNull(PasswordManagementWebflowUtils.getPasswordResetQuestions(context, Map.class));

        assertEquals(0, accountProfileServiceTicketGeneratorAuthority.getOrder());
        assertFalse(accountProfileServiceTicketGeneratorAuthority.shouldGenerate(
            mock(AuthenticationResult.class), mock(Service.class)));
        val url = Strings.CI.appendIfMissing(casProperties.getServer().getPrefix(), "/")
            .concat(CasWebflowConfigurer.FLOW_ID_ACCOUNT);
        val service = RegisteredServiceTestUtils.getService(url);
        assertTrue(accountProfileServiceTicketGeneratorAuthority.supports(
            mock(AuthenticationResult.class), service));
    }
}
