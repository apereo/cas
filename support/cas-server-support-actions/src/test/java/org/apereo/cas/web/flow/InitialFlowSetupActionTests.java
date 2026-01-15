package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.core.sso.SingleSignOnProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceAuthenticationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.cookie.CookieGenerationContext;
import org.apereo.cas.web.flow.login.InitialFlowSetupAction;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.web.support.mgmr.NoOpCookieValueManager;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("WebflowActions")
class InitialFlowSetupActionTests {

    @Nested
    class DefaultTests extends AbstractWebflowActionsTests {

        private static final String CONST_CONTEXT_PATH = "/test";

        private static final String CONST_CONTEXT_PATH_2 = "/test1";

        @Autowired
        @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
        private AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

        @Autowired
        @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
        private AuthenticationEventExecutionPlan authenticationEventExecutionPlan;

        @Autowired
        @Qualifier(ArgumentExtractor.BEAN_NAME)
        private ArgumentExtractor argumentExtractor;

        private InitialFlowSetupAction action;

        private CasCookieBuilder warnCookieGenerator;

        private CasCookieBuilder tgtCookieGenerator;

        @BeforeEach
        void initialize() throws Exception {
            val warn = CookieGenerationContext.builder()
                .name(CasWebflowConstants.ATTRIBUTE_WARN_ON_REDIRECT)
                .path(StringUtils.EMPTY)
                .maxAge(2)
                .domain(null)
                .secure(false)
                .httpOnly(false)
                .build();

            val tgt = CookieGenerationContext.builder()
                .name("tgt")
                .path(StringUtils.EMPTY)
                .maxAge(2)
                .domain(null)
                .secure(false)
                .httpOnly(false)
                .build();

            val cookieValueManager = new NoOpCookieValueManager(tenantExtractor);
            warnCookieGenerator = CookieUtils.buildCookieRetrievingGenerator(cookieValueManager, warn);
            warnCookieGenerator.setCookiePath(StringUtils.EMPTY);
            tgtCookieGenerator = CookieUtils.buildCookieRetrievingGenerator(cookieValueManager, tgt);
            tgtCookieGenerator.setCookiePath(StringUtils.EMPTY);

            val servicesManager = mock(ServicesManager.class);
            when(servicesManager.findServiceBy(any(Service.class))).thenReturn(RegisteredServiceTestUtils.getRegisteredService("test"));
            val sso = new SingleSignOnProperties().setCreateSsoCookieOnRenewAuthn(true).setRenewAuthnEnabled(true);
            action = new InitialFlowSetupAction(List.of(argumentExtractor), servicesManager,
                authenticationRequestServiceSelectionStrategies, tgtCookieGenerator,
                warnCookieGenerator, casProperties, authenticationEventExecutionPlan,
                new DefaultSingleSignOnParticipationStrategy(servicesManager, sso, mock(TicketRegistrySupport.class), mock(AuthenticationServiceSelectionPlan.class)),
                mock(TicketRegistrySupport.class));
            action.afterPropertiesSet();
        }

        @Test
        void verifySettingContextPath() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setContextPath(CONST_CONTEXT_PATH);

            action.execute(context);

            assertEquals(CONST_CONTEXT_PATH + '/', warnCookieGenerator.getCookiePath());
            assertEquals(CONST_CONTEXT_PATH + '/', tgtCookieGenerator.getCookiePath());
        }

        @Test
        void verifyResettingContextPath() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setContextPath(CONST_CONTEXT_PATH);
            
            action.execute(context);

            assertEquals(CONST_CONTEXT_PATH + '/', warnCookieGenerator.getCookiePath());
            assertEquals(CONST_CONTEXT_PATH + '/', tgtCookieGenerator.getCookiePath());

            context.setContextPath(CONST_CONTEXT_PATH_2);
            action.execute(context);

            assertNotSame(CONST_CONTEXT_PATH_2 + '/', warnCookieGenerator.getCookiePath());
            assertNotSame(CONST_CONTEXT_PATH_2 + '/', tgtCookieGenerator.getCookiePath());
            assertEquals(CONST_CONTEXT_PATH + '/', warnCookieGenerator.getCookiePath());
            assertEquals(CONST_CONTEXT_PATH + '/', tgtCookieGenerator.getCookiePath());
        }
    }

    @TestPropertySource(properties = {
        "cas.authn.policy.source-selection-enabled=true",
        "cas.sso.sso-enabled=false",
        "cas.tgc.crypto.enabled=false"
    })
    @Nested
    class SsoDisabledTests extends AbstractWebflowActionsTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP)
        private Action action;

        @Test
        void verifyResponseStatusAsError() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.getHttpServletResponse().setStatus(HttpStatus.UNAUTHORIZED.value());
            assertThrows(UnauthorizedServiceException.class, () -> action.execute(context));
        }

        @Test
        void verifyNoServiceFound() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val event = action.execute(context);
            assertNull(WebUtils.getService(context));
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        }

        @Test
        void verifyServiceFound() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "test");
            val event = action.execute(context);

            assertEquals("test", WebUtils.getService(context).getId());
            assertNotNull(WebUtils.getRegisteredService(context));
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        }

        @Test
        void verifyServiceStrategy() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setMethod(HttpMethod.POST);

            val id = UUID.randomUUID().toString();
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(id);
            val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
            accessStrategy.setUnauthorizedRedirectUrl(new URI("https://apereo.org/cas"));
            registeredService.setAccessStrategy(accessStrategy);
            context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, id);
            getServicesManager().save(registeredService);


            val event = action.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        }

        @Test
        void verifyTgtNoSso() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val tgt = new MockTicketGrantingTicket("casuser");
            getTicketRegistry().addTicket(tgt);
            getTicketGrantingTicketCookieGenerator().addCookie(context.getHttpServletRequest(), context.getHttpServletResponse(), tgt.getId());
            context.setRequestCookiesFromResponse();

            val event = action.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
            assertTrue(WebUtils.isExistingSingleSignOnSessionAvailable(context));
            assertNotNull(getTicketRegistry().getTicket(tgt.getId()));
        }


        @Test
        void verifyAuthHandlersSelected() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val id = UUID.randomUUID().toString();
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(id);
            registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy());
            registeredService.setAuthenticationPolicy(new DefaultRegisteredServiceAuthenticationPolicy()
                .setRequiredAuthenticationHandlers(CollectionUtils.wrapHashSet("handler1", "handler2")));
            getServicesManager().save(registeredService);
            context.setParameter(CasProtocolConstants.PARAMETER_SERVICE, id);

            val event = action.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
            val handlers = WebUtils.getAvailableAuthenticationHandleNames(context);
            assertTrue(handlers.isEmpty());
        }
    }
}
