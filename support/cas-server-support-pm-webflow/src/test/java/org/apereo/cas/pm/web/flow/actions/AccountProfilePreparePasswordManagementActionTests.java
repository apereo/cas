package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ServiceTicketGeneratorAuthority;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.config.CasWebflowAccountProfileConfiguration;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AccountProfilePreparePasswordManagementActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("WebflowActions")
@TestPropertySource(properties = {
    "cas.authn.pm.groovy.location=classpath:PasswordManagementService.groovy",
    "cas.authn.pm.core.enabled=true",
    "cas.authn.pm.reset.security-questions-enabled=true",
    "CasFeatureModule.AccountManagement.enabled=true"
})
@Import(CasWebflowAccountProfileConfiguration.class)
public class AccountProfilePreparePasswordManagementActionTests extends BasePasswordManagementActionTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_PREPARE_ACCOUNT_PASSWORD_MANAGEMENT)
    private Action prepareAccountProfilePasswordMgmtAction;

    @Autowired
    @Qualifier("accountProfileServiceTicketGeneratorAuthority")
    private ServiceTicketGeneratorAuthority accountProfileServiceTicketGeneratorAuthority;

    @Test
    public void verifyOperation() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val tgt = new MockTicketGrantingTicket("casuser");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        WebUtils.putTicketGrantingTicket(context, tgt);
        ticketRegistry.addTicket(tgt);
        val result = prepareAccountProfilePasswordMgmtAction.execute(context);
        assertNull(result);
        assertTrue(WebUtils.isPasswordManagementEnabled(context));
        assertNotNull(PasswordManagementWebflowUtils.getPasswordResetQuestions(context, Map.class));

        assertEquals(0, accountProfileServiceTicketGeneratorAuthority.getOrder());
        assertFalse(accountProfileServiceTicketGeneratorAuthority.shouldGenerate(
            mock(AuthenticationResult.class), mock(Service.class)));
        val url = StringUtils.appendIfMissing(casProperties.getServer().getPrefix(), "/")
            .concat(CasWebflowConfigurer.FLOW_ID_ACCOUNT);
        val service = RegisteredServiceTestUtils.getService(url);
        assertTrue(accountProfileServiceTicketGeneratorAuthority.supports(
            mock(AuthenticationResult.class), service));
    }
}
