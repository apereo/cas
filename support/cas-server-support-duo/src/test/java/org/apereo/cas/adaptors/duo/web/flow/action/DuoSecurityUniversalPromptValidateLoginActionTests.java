package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.adaptors.duo.BaseDuoSecurityTests;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilder;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pac4j.BrowserWebStorageSessionStore;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.web.BrowserSessionStorage;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import com.duosecurity.Client;
import com.duosecurity.model.AccessDevice;
import com.duosecurity.model.Application;
import com.duosecurity.model.AuthContext;
import com.duosecurity.model.AuthDevice;
import com.duosecurity.model.AuthResult;
import com.duosecurity.model.Location;
import com.duosecurity.model.Token;
import com.duosecurity.model.User;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;
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
        "cas.authn.mfa.duo[0].duo-integration-key=DIOXVRZD2UMZ8XXMNFQ5",
        "cas.authn.mfa.duo[0].trusted-device-enabled=true",
        "cas.authn.mfa.duo[0].duo-api-host=theapi.duosecurity.com"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("DuoSecurity")
class DuoSecurityUniversalPromptValidateLoginActionTests extends BaseCasWebflowMultifactorAuthenticationTests {
    @Autowired
    @Qualifier("duoUniversalPromptSessionStore")
    private BrowserWebStorageSessionStore duoUniversalPromptSessionStore;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DUO_UNIVERSAL_PROMPT_VALIDATE_LOGIN)
    private Action duoUniversalPromptValidateLoginAction;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DUO_UNIVERSAL_PROMPT_PREPARE_LOGIN)
    private Action duoUniversalPromptPrepareLoginAction;

    @Autowired
    private ConfigurableApplicationContext configurableApplicationContext;

    @Test
    void verifySkip() throws Exception {
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
    void verifyError() throws Exception {
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
        assertEquals(CasWebflowConstants.TRANSITION_ID_RESTORE, result.getId());
    }

    @Test
    void verifyPass() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val webContext = new JEEContext(request, response);

        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val identifier = casProperties.getAuthn().getMfa().getDuo().get(0).getId();
        val provider = TestMultifactorAuthenticationProvider
            .registerProviderIntoApplicationContext(applicationContext, new TestMultifactorAuthenticationProvider(identifier));

        configurableApplicationContext.getBeansOfType(MultifactorAuthenticationPrincipalResolver.class)
            .forEach((key, value) -> ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext, value, key));

        val authentication = RegisteredServiceTestUtils.getAuthentication();
        WebUtils.putAuthentication(authentication, context);
        WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService());
        WebUtils.putMultifactorAuthenticationProvider(context, provider);
        WebUtils.putTargetTransition(context, "targetDestination");

        val authnResult = new DefaultAuthenticationResultBuilder()
            .collect(RegisteredServiceTestUtils.getAuthentication());

        WebUtils.putAuthenticationResultBuilder(authnResult, context);
        context.getFlashScope().put("name", "value");
        context.getConversationScope().put("name", "value");
        context.getRequestScope().put("name", "value");

        val prepResult = duoUniversalPromptPrepareLoginAction.execute(context);

        val storage = (BrowserSessionStorage) prepResult.getAttributes().get("result");
        val attributes = duoUniversalPromptSessionStore.buildFromTrackableSession(webContext, storage)
            .map(BrowserWebStorageSessionStore.class::cast)
            .orElseThrow()
            .getSessionAttributes();

        val code = UUID.randomUUID().toString();
        request.addParameter(DuoSecurityUniversalPromptValidateLoginAction.REQUEST_PARAMETER_CODE, code);
        request.addParameter(DuoSecurityUniversalPromptValidateLoginAction.REQUEST_PARAMETER_STATE,
            attributes.get(DuoSecurityAuthenticationService.class.getSimpleName()).toString());
        request.addParameter(BrowserSessionStorage.KEY_SESSION_STORAGE, storage.getPayload());

        val result = duoUniversalPromptValidateLoginAction.execute(context);
        assertNotNull(result);
        assertEquals("targetDestination", result.getId());
        assertNotNull(WebUtils.getAuthentication(context));
        assertNotNull(WebUtils.getRegisteredService(context));
        assertNotNull(WebUtils.getAuthenticationResult(context));
    }

    @TestConfiguration(value = "DuoSecurityUniversalPromptValidateLoginActionTestConfiguration", proxyBeanMethods = false)
    static class DuoSecurityUniversalPromptValidateLoginActionTestConfiguration {
        @Bean
        public Client duoUniversalPromptAuthenticationClient() throws Exception {
            val token = new Token();
            token.setSub("casuser");

            val user = new User();
            user.setKey(UUID.randomUUID().toString());
            user.setName("casuser");
            
            val authCtx = new AuthContext();


            val application = new Application();
            application.setKey(UUID.randomUUID().toString());
            application.setName("CAS");
            authCtx.setApplication(application);
            authCtx.setUser(user);
            authCtx.setEvent_type("auth");

            val accessDevice = new AccessDevice();
            accessDevice.setIp("1.2.3.4");

            val location = new Location();
            location.setCity("London");
            location.setCountry("UK");
            accessDevice.setLocation(location);
            authCtx.setAccess_device(accessDevice);

            val authDevice = new AuthDevice();
            authDevice.setLocation(location);
            authDevice.setIp("1.2.3.4");
            authCtx.setAuth_device(authDevice);

            token.setAuth_context(authCtx);

            val authResult = new AuthResult();
            authResult.setResult("OK");
            token.setAuth_result(authResult);

            val duoClient = mock(Client.class);
            when(duoClient.generateState()).thenReturn(UUID.randomUUID().toString());
            when(duoClient.createAuthUrl(anyString(), anyString())).thenReturn("https://duo.com");
            when(duoClient.exchangeAuthorizationCodeFor2FAResult(anyString(), anyString())).thenReturn(token);
            return duoClient;
        }
    }
}
