package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.adaptors.duo.BaseDuoSecurityTests;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationResult;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBean;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import com.duosecurity.Client;
import com.duosecurity.model.Token;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DuoSecurityUniversalPromptValidateLoginActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = {
    DuoSecurityUniversalPromptValidateLoginActionTests.DuoSecurityUniversalPromptValidateLoginActionTestConfiguration.class,
    BaseDuoSecurityTests.SharedTestConfiguration.class
},
    properties = {
        "cas.authn.mfa.duo[0].duo-secret-key=Q2IU2i8BFNd6VYflZT8Evl6lF7oPlj3PM15BmRU7",
        "cas.authn.mfa.duo[0].mode=UNIVERSAL",
        "cas.authn.mfa.duo[0].duo-integration-key=DIOXVRZD2UMZ8XXMNFQ5",
        "cas.authn.mfa.duo[0].trusted-device-enabled=true",
        "cas.authn.mfa.duo[0].duo-api-host=theapi.duosecurity.com"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("WebflowMfaActions")
public class DuoSecurityUniversalPromptValidateLoginActionTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("duoUniversalPromptValidateLoginAction")
    private Action duoUniversalPromptValidateLoginAction;

    @Autowired
    @Qualifier("duoUniversalPromptPrepareLoginAction")
    private Action duoUniversalPromptPrepareLoginAction;

    @Test
    public void verifySkip() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val result = duoUniversalPromptValidateLoginAction.execute(context);
        assertNotNull(result);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SKIP, result.getId());
    }

    @Test
    public void verifyError() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        request.addParameter(DuoSecurityUniversalPromptValidateLoginAction.REQUEST_PARAMETER_CODE, "bad-code");
        request.addParameter(DuoSecurityUniversalPromptValidateLoginAction.REQUEST_PARAMETER_STATE, "bad-state");
        val result = duoUniversalPromptValidateLoginAction.execute(context);
        assertNotNull(result);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());
    }

    @Test
    public void verifyPass() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val identifier = casProperties.getAuthn().getMfa().getDuo().get(0).getId();
        val provider = TestMultifactorAuthenticationProvider
            .registerProviderIntoApplicationContext(applicationContext, new TestMultifactorAuthenticationProvider(identifier));

        val authentication = RegisteredServiceTestUtils.getAuthentication();
        WebUtils.putAuthentication(authentication, context);
        WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService());
        WebUtils.putMultifactorAuthenticationProviderIdIntoFlowScope(context, provider);
        val builder = mock(AuthenticationResultBuilder.class);
        when(builder.getInitialAuthentication()).thenReturn(Optional.of(authentication));
        when(builder.collect(any(Authentication.class))).thenReturn(builder);
        WebUtils.putAuthenticationResultBuilder(builder, context);
        val prepResult = duoUniversalPromptPrepareLoginAction.execute(context);

        val ticket = (TransientSessionTicket) prepResult.getAttributes().get("result");
        val code = UUID.randomUUID().toString();

        request.addParameter(DuoSecurityUniversalPromptValidateLoginAction.REQUEST_PARAMETER_CODE, code);
        request.addParameter(DuoSecurityUniversalPromptValidateLoginAction.REQUEST_PARAMETER_STATE, ticket.getId());
        val result = duoUniversalPromptValidateLoginAction.execute(context);
        assertNotNull(result);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
    }

    @TestConfiguration("DuoSecurityUniversalPromptValidateLoginActionTestConfiguration")
    public static class DuoSecurityUniversalPromptValidateLoginActionTestConfiguration {
        @Bean
        public MultifactorAuthenticationProviderBean
            <DuoSecurityMultifactorAuthenticationProvider, DuoSecurityMultifactorAuthenticationProperties> duoProviderBean() throws Exception {

            val token = new Token();
            token.setSub("casuser");
            val duoClient = mock(Client.class);
            when(duoClient.generateState()).thenReturn(UUID.randomUUID().toString());
            when(duoClient.createAuthUrl(anyString(), anyString())).thenReturn("https://duo.com");
            when(duoClient.exchangeAuthorizationCodeFor2FAResult(anyString(), anyString())).thenReturn(token);
            val duoAuthService = mock(DuoSecurityAuthenticationService.class);
            when(duoAuthService.getDuoClient()).thenReturn(Optional.of(duoClient));
            when(duoAuthService.authenticate(any()))
                .thenReturn(DuoSecurityAuthenticationResult.builder().success(true).username("casuser").build());

            val provider = mock(DuoSecurityMultifactorAuthenticationProvider.class);
            when(provider.getId()).thenReturn(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
            when(provider.createUniqueId()).thenReturn(DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER);
            when(provider.validateId(anyString())).thenReturn(Boolean.TRUE);
            when(provider.getDuoAuthenticationService()).thenReturn(duoAuthService);
            val bean = mock(MultifactorAuthenticationProviderBean.class);
            when(bean.getProvider(anyString())).thenReturn(provider);
            return bean;
        }
    }
}
