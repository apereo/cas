package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.adaptors.duo.BaseDuoSecurityTests;
import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;
import org.apereo.cas.adaptors.duo.DuoSecurityUserAccountStatus;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationRegistrationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.http.client.utils.URIBuilder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DuoSecurityDetermineUserAccountActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseDuoSecurityTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.duo[0].duo-secret-key=1234567890",
        "cas.authn.mfa.duo[0].duo-application-key=abcdefghijklmnop",
        "cas.authn.mfa.duo[0].duo-integration-key=QRSTUVWXYZ",
        "cas.authn.mfa.duo[0].duo-api-host=theapi.duosecurity.com"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("DuoSecurity")
public class DuoSecurityDetermineUserAccountActionTests extends BaseCasWebflowMultifactorAuthenticationTests {

    @Autowired
    @Qualifier("determineDuoUserAccountAction")
    private Action determineDuoUserAccountAction;

    @SneakyThrows
    private RequestContext verifyOperation(final DuoSecurityUserAccountStatus status, final String eventId) {
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
        when(provider.getId()).thenReturn(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
        when(provider.getDuoAuthenticationService()).thenReturn(duoService);

        val registration = new DuoSecurityMultifactorAuthenticationRegistrationProperties()
            .setRegistrationUrl("https://registration.duo.com");
        registration.getCrypto().setEnabled(true);

        servicesManager.save(RegisteredServiceTestUtils.getRegisteredService("registration.duo.com"));
        
        when(provider.getRegistration()).thenReturn(registration);
        when(provider.matches(anyString())).thenReturn(Boolean.TRUE);

        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            MultifactorAuthenticationPrincipalResolver.identical(), UUID.randomUUID().toString());

        WebUtils.putMultifactorAuthenticationProviderIdIntoFlowScope(context, provider);
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext, provider);

        val event = determineDuoUserAccountAction.execute(context);
        assertEquals(eventId, event.getId());
        return context;
    }

    @Test
    public void verifyOperationEnroll() throws Exception {
        val context = verifyOperation(DuoSecurityUserAccountStatus.ENROLL, CasWebflowConstants.TRANSITION_ID_ENROLL);
        val url = context.getFlowScope().get("duoRegistrationUrl", String.class);
        assertNotNull(url);
        assertTrue(new URIBuilder(url).getQueryParams().get(0).getName().equalsIgnoreCase("principal"));
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
