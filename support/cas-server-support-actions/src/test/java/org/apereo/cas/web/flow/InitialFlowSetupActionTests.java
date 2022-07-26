package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.configuration.model.core.sso.SingleSignOnProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.cookie.CookieGenerationContext;
import org.apereo.cas.web.flow.login.InitialFlowSetupAction;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.DefaultArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.web.support.gen.CookieRetrievingCookieGenerator;

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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import java.net.URI;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Tag("WebflowActions")
public class InitialFlowSetupActionTests {

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class DefaultTests extends AbstractWebflowActionsTests {

        private static final String CONST_CONTEXT_PATH = "/test";

        private static final String CONST_CONTEXT_PATH_2 = "/test1";

        @Autowired
        @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
        private AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

        @Autowired
        @Qualifier(AuthenticationEventExecutionPlan.DEFAULT_BEAN_NAME)
        private AuthenticationEventExecutionPlan authenticationEventExecutionPlan;

        private InitialFlowSetupAction action;

        private CasCookieBuilder warnCookieGenerator;

        private CasCookieBuilder tgtCookieGenerator;

        @BeforeEach
        public void initialize() throws Exception {
            val warn = CookieGenerationContext.builder()
                .name("warn")
                .path(StringUtils.EMPTY)
                .maxAge(2)
                .domain(null)
                .secure(false)
                .httpOnly(false)
                .comment("CAS Cookie")
                .build();

            val tgt = CookieGenerationContext.builder()
                .name("tgt")
                .path(StringUtils.EMPTY)
                .maxAge(2)
                .domain(null)
                .secure(false)
                .httpOnly(false)
                .comment("CAS Cookie")
                .build();

            this.warnCookieGenerator = new CookieRetrievingCookieGenerator(warn);
            this.warnCookieGenerator.setCookiePath(StringUtils.EMPTY);
            this.tgtCookieGenerator = new CookieRetrievingCookieGenerator(tgt);
            this.tgtCookieGenerator.setCookiePath(StringUtils.EMPTY);

            val argExtractors = Collections.<ArgumentExtractor>singletonList(new DefaultArgumentExtractor(new WebApplicationServiceFactory()));
            val servicesManager = mock(ServicesManager.class);
            when(servicesManager.findServiceBy(any(Service.class))).thenReturn(RegisteredServiceTestUtils.getRegisteredService("test"));
            val sso = new SingleSignOnProperties().setCreateSsoCookieOnRenewAuthn(true).setRenewAuthnEnabled(true);
            action = new InitialFlowSetupAction(argExtractors, servicesManager,
                authenticationRequestServiceSelectionStrategies, tgtCookieGenerator,
                warnCookieGenerator, casProperties, authenticationEventExecutionPlan,
                new DefaultSingleSignOnParticipationStrategy(servicesManager, sso, mock(TicketRegistrySupport.class), mock(AuthenticationServiceSelectionPlan.class)),
                mock(TicketRegistrySupport.class));
            action.afterPropertiesSet();
        }

        @Test
        public void verifySettingContextPath() {
            val request = new MockHttpServletRequest();
            request.setContextPath(CONST_CONTEXT_PATH);
            val context = new MockRequestContext();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

            action.doExecute(context);

            assertEquals(CONST_CONTEXT_PATH + '/', this.warnCookieGenerator.getCookiePath());
            assertEquals(CONST_CONTEXT_PATH + '/', this.tgtCookieGenerator.getCookiePath());
        }

        @Test
        public void verifyResettingContextPath() {
            val request = new MockHttpServletRequest();
            request.setContextPath(CONST_CONTEXT_PATH);
            val context = new MockRequestContext();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

            this.action.doExecute(context);

            assertEquals(CONST_CONTEXT_PATH + '/', this.warnCookieGenerator.getCookiePath());
            assertEquals(CONST_CONTEXT_PATH + '/', this.tgtCookieGenerator.getCookiePath());

            request.setContextPath(CONST_CONTEXT_PATH_2);
            this.action.doExecute(context);

            assertNotSame(CONST_CONTEXT_PATH_2 + '/', this.warnCookieGenerator.getCookiePath());
            assertNotSame(CONST_CONTEXT_PATH_2 + '/', this.tgtCookieGenerator.getCookiePath());
            assertEquals(CONST_CONTEXT_PATH + '/', this.warnCookieGenerator.getCookiePath());
            assertEquals(CONST_CONTEXT_PATH + '/', this.tgtCookieGenerator.getCookiePath());
        }
    }

    @TestPropertySource(properties = {
        "cas.authn.policy.source-selection-enabled=true",
        "cas.sso.sso-enabled=false",
        "cas.tgc.crypto.enabled=false"
    })
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class SsoDisabledTests extends AbstractWebflowActionsTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP)
        private Action action;

        @Test
        public void verifyResponseStatusAsError() throws Exception {
            val context = new MockRequestContext();
            var response = new MockHttpServletResponse();
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            context.setExternalContext(new ServletExternalContext(new MockServletContext(),
                new MockHttpServletRequest(), response));
            assertThrows(UnauthorizedServiceException.class, () -> action.execute(context));
        }

        @Test
        public void verifyNoServiceFound() throws Exception {
            val context = new MockRequestContext();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(),
                new MockHttpServletRequest(), new MockHttpServletResponse()));
            val event = this.action.execute(context);
            assertNull(WebUtils.getService(context));
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        }

        @Test
        public void verifyServiceFound() throws Exception {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, "test");
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

            val event = this.action.execute(context);

            assertEquals("test", WebUtils.getService(context).getId());
            assertNotNull(WebUtils.getRegisteredService(context));
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        }

        @Test
        public void verifyServiceStrategy() throws Exception {
            val response = new MockHttpServletResponse();
            val request = new MockHttpServletRequest();
            request.setMethod(HttpMethod.POST.name());

            val id = UUID.randomUUID().toString();
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(id);
            val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
            accessStrategy.setUnauthorizedRedirectUrl(new URI("https://apereo.org/cas"));
            registeredService.setAccessStrategy(accessStrategy);
            request.setParameter(CasProtocolConstants.PARAMETER_SERVICE, id);
            getServicesManager().save(registeredService);

            val context = new MockRequestContext();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

            val event = this.action.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
        }

        @Test
        public void verifyTgtNoSso() throws Exception {
            val response = new MockHttpServletResponse();
            val request = new MockHttpServletRequest();

            val tgt = new MockTicketGrantingTicket("casuser");
            getTicketRegistry().addTicket(tgt);
            getTicketGrantingTicketCookieGenerator().addCookie(response, tgt.getId());
            request.setCookies(response.getCookies());

            val context = new MockRequestContext();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

            val event = this.action.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
            assertTrue(WebUtils.isExistingSingleSignOnSessionAvailable(context));
            assertNull(getTicketRegistry().getTicket(tgt.getId()));
        }


    }
}
