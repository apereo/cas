package org.apereo.cas.adaptors.duo.web.flow;

import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;
import org.apereo.cas.adaptors.duo.DuoSecurityUserAccountStatus;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.duo.web.flow.action.DuoSecurityDetermineUserAccountAction;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorProperties;
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DuoSecurityDetermineUserAccountActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Webflow")
public class DuoSecurityDetermineUserAccountActionTests {

    @SneakyThrows
    private static void verifyOperation(final DuoSecurityUserAccountStatus status, final String eventId) {

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        val authentication = CoreAuthenticationTestUtils.getAuthentication();
        WebUtils.putAuthentication(authentication, context);

        val account = new DuoSecurityUserAccount(authentication.getPrincipal().getId());
        account.setStatus(status);
        account.setEnrollPortalUrl("https://example.org");

        val duoService = mock(DuoSecurityAuthenticationService.class);
        when(duoService.getUserAccount(anyString())).thenReturn(account);
        val provider = mock(DuoSecurityMultifactorAuthenticationProvider.class);
        when(provider.getId()).thenReturn(DuoSecurityMultifactorProperties.DEFAULT_IDENTIFIER);
        when(provider.getDuoAuthenticationService()).thenReturn(duoService);
        when(provider.getRegistrationUrl()).thenReturn("https://registration.duo.com");
        when(provider.matches(anyString())).thenReturn(Boolean.TRUE);

        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        WebUtils.putMultifactorAuthenticationProviderIdIntoFlowScope(context, provider);
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext, provider);

        val action = new DuoSecurityDetermineUserAccountAction(applicationContext);
        val event = action.execute(context);
        assertEquals(eventId, event.getId());
    }

    @Test
    public void verifyOperationEnroll() {
        verifyOperation(DuoSecurityUserAccountStatus.ENROLL, CasWebflowConstants.TRANSITION_ID_ENROLL);
    }

    @Test
    public void verifyOperationAllow() {
        verifyOperation(DuoSecurityUserAccountStatus.ALLOW, CasWebflowConstants.TRANSITION_ID_BYPASS);
    }

    @Test
    public void verifyOperationDeny() {
        verifyOperation(DuoSecurityUserAccountStatus.DENY, CasWebflowConstants.TRANSITION_ID_DENY);
    }

    @Test
    public void verifyOperationUnavailable() {
        verifyOperation(DuoSecurityUserAccountStatus.UNAVAILABLE, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
    }

    @Test
    public void verifyOperationAuth() {
        verifyOperation(DuoSecurityUserAccountStatus.AUTH, CasWebflowConstants.TRANSITION_ID_SUCCESS);
    }
}
