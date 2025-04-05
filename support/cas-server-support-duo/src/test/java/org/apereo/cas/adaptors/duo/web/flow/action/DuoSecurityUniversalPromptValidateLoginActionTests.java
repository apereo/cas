package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.adaptors.duo.BaseDuoSecurityTests;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityAuthenticationService;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilder;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.duo.DuoSecurityMultifactorAuthenticationProperties;
import org.apereo.cas.pac4j.BrowserWebStorageSessionStore;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.BrowserStorage;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.execution.Action;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DuoSecurityUniversalPromptValidateLoginActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */

@Tag("DuoSecurity")
@ExtendWith(CasTestExtension.class)
class DuoSecurityUniversalPromptValidateLoginActionTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).minimal(false).build().toObjectMapper();

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
    abstract static class BaseTests extends BaseCasWebflowMultifactorAuthenticationTests {
        @Autowired
        @Qualifier("duoUniversalPromptSessionStore")
        protected BrowserWebStorageSessionStore duoUniversalPromptSessionStore;

        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_DUO_UNIVERSAL_PROMPT_VALIDATE_LOGIN)
        protected Action duoUniversalPromptValidateLoginAction;

        @Autowired
        @Qualifier(PrincipalElectionStrategy.BEAN_NAME)
        protected PrincipalElectionStrategy principalElectionStrategy;

        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_DUO_UNIVERSAL_PROMPT_PREPARE_LOGIN)
        protected Action duoUniversalPromptPrepareLoginAction;
    }

    @Nested
    class DefaultTests extends BaseTests {
        @Test
        void verifySkip() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val result = duoUniversalPromptValidateLoginAction.execute(context);
            assertNotNull(result);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SKIP, result.getId());
        }

        @Test
        void verifyRestoreWithoutStorage() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val mockedState = mock(State.class);
            when(mockedState.getId()).thenReturn(CasWebflowConstants.STATE_ID_DUO_UNIVERSAL_PROMPT_VALIDATE_LOGIN);

            context.setCurrentState(mockedState);
            context.setParameter(DuoSecurityUniversalPromptValidateLoginAction.REQUEST_PARAMETER_CODE, UUID.randomUUID().toString());
            context.setParameter(DuoSecurityUniversalPromptValidateLoginAction.REQUEST_PARAMETER_STATE, UUID.randomUUID().toString());
            val result = duoUniversalPromptValidateLoginAction.execute(context);
            assertNotNull(result);
            assertEquals(CasWebflowConstants.TRANSITION_ID_RESTORE, result.getId());

            assertEquals(CasWebflowConstants.STATE_ID_DUO_UNIVERSAL_PROMPT_VALIDATE_LOGIN, WebUtils.getTargetState(context));
            assertEquals(CasWebflowConstants.TRANSITION_ID_SWITCH, WebUtils.getTargetTransition(context));
            assertEquals(duoUniversalPromptSessionStore.getBrowserStorageContextKey(),
                WebUtils.getBrowserStorageContextKey(context, duoUniversalPromptSessionStore.getBrowserStorageContextKey()));
        }

        @Test
        void verifyError() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setParameter(DuoSecurityUniversalPromptValidateLoginAction.REQUEST_PARAMETER_CODE, "bad-code");
            context.setParameter(DuoSecurityUniversalPromptValidateLoginAction.REQUEST_PARAMETER_STATE, "bad-state");
            val result = duoUniversalPromptValidateLoginAction.execute(context);
            assertNotNull(result);
            assertEquals(CasWebflowConstants.TRANSITION_ID_RESTORE, result.getId());
        }

        @Test
        void verifyPass() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val webContext = new JEEContext(context.getHttpServletRequest(), context.getHttpServletResponse());

            val authentication = RegisteredServiceTestUtils.getAuthentication();
            WebUtils.putAuthentication(authentication, context);
            WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService());

            val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(
                DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER, applicationContext).orElseThrow();
            MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, provider);
            WebUtils.putTargetTransition(context, "targetDestination");

            val authnResult = new DefaultAuthenticationResultBuilder(principalElectionStrategy)
                .collect(RegisteredServiceTestUtils.getAuthentication());

            WebUtils.putAuthenticationResultBuilder(authnResult, context);
            context.getFlashScope().put("name", "value");
            context.getConversationScope().put("name", "value");
            context.getRequestScope().put("name", "value");

            val prepResult = duoUniversalPromptPrepareLoginAction.execute(context);

            val storage = (BrowserStorage) prepResult.getAttributes().get("result");
            val payload = MAPPER.writeValueAsString(Map.of(storage.getContext(), storage.getPayload()));
            val attributes = duoUniversalPromptSessionStore.buildFromTrackableSession(webContext, payload)
                .map(BrowserWebStorageSessionStore.class::cast)
                .orElseThrow()
                .getSessionAttributes(webContext);

            val code = UUID.randomUUID().toString();
            context.setParameter(DuoSecurityUniversalPromptValidateLoginAction.REQUEST_PARAMETER_CODE, code);
            context.setParameter(DuoSecurityUniversalPromptValidateLoginAction.REQUEST_PARAMETER_STATE,
                attributes.get(DuoSecurityAuthenticationService.class.getSimpleName()).toString());
            context.setParameter(BrowserStorage.PARAMETER_BROWSER_STORAGE, payload);

            val result = duoUniversalPromptValidateLoginAction.execute(context);
            assertNotNull(result);
            assertEquals("targetDestination", result.getId());
            assertNotNull(WebUtils.getAuthentication(context));
            assertNotNull(WebUtils.getRegisteredService(context));
            assertNotNull(WebUtils.getAuthenticationResult(context));
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.mfa.duo[0].session-storage-type=TICKET_REGISTRY")
    class TicketRegistryStorageTests extends BaseTests {
        @Test
        public void verifyPass() throws Exception {
            val context = MockRequestContext.create(applicationContext);
            val authentication = RegisteredServiceTestUtils.getAuthentication();
            WebUtils.putAuthentication(authentication, context);
            WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService());
            
            val provider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(
                DuoSecurityMultifactorAuthenticationProperties.DEFAULT_IDENTIFIER, applicationContext).orElseThrow();
            MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, provider);

            val authnResult = new DefaultAuthenticationResultBuilder(principalElectionStrategy)
                .collect(RegisteredServiceTestUtils.getAuthentication());

            WebUtils.putAuthenticationResultBuilder(authnResult, context);

            val prepResult = duoUniversalPromptPrepareLoginAction.execute(context);

            val ticket = ticketRegistry.getTicket(prepResult.getAttributes().get("result").toString(),
                TransientSessionTicket.class);
            val code = UUID.randomUUID().toString();

            context.setParameter(DuoSecurityUniversalPromptValidateLoginAction.REQUEST_PARAMETER_CODE, code);
            context.setParameter(DuoSecurityUniversalPromptValidateLoginAction.REQUEST_PARAMETER_STATE,
                TransientSessionTicket.PREFIX + "-1234567");
            var result = duoUniversalPromptValidateLoginAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());
            
            context.setParameter(DuoSecurityUniversalPromptValidateLoginAction.REQUEST_PARAMETER_STATE, ticket.getId());
            result = duoUniversalPromptValidateLoginAction.execute(context);
            assertNotNull(result);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
            assertNotNull(WebUtils.getAuthentication(context));
            assertNotNull(WebUtils.getRegisteredService(context));
            assertNotNull(WebUtils.getAuthenticationResult(context));
        }
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
