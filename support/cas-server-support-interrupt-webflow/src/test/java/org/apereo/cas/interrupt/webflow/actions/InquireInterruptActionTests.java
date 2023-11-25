package org.apereo.cas.interrupt.webflow.actions;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlanConfigurer;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.interrupt.InterruptTrackingEngine;
import org.apereo.cas.interrupt.webflow.InterruptUtils;
import org.apereo.cas.services.BaseWebBasedRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceWebflowInterruptPolicy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.model.TriStateBoolean;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link InquireInterruptActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("WebflowActions")
class InquireInterruptActionTests {

    @Nested
    @SpringBootTest(classes = {
        InterruptActiveTestConfiguration.class,
        BaseInterruptFlowActionTests.SharedTestConfiguration.class
    })
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    class InterruptActiveTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_INQUIRE_INTERRUPT)
        private Action action;

        @Autowired
        @Qualifier(InterruptTrackingEngine.BEAN_NAME)
        private InterruptTrackingEngine interruptTrackingEngine;

        @Test
        void verifyInterruptedByServicePrincipalAttribute() throws Throwable {
            val context = MockRequestContext.create();
            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);

            val registeredService = (BaseWebBasedRegisteredService) RegisteredServiceTestUtils.getRegisteredService();
            val webflowInterruptPolicy = new DefaultRegisteredServiceWebflowInterruptPolicy()
                .setAttributeName("mem...of").setAttributeValue("^st[a-z]ff$");
            registeredService.setWebflowInterruptPolicy(webflowInterruptPolicy);
            WebUtils.putRegisteredService(context, registeredService);
            WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
            WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());

            val event = action.execute(context);
            assertNotNull(InterruptUtils.getInterruptFrom(context));
            assertNotNull(WebUtils.getPrincipalFromRequestContext(context));
            assertEquals(CasWebflowConstants.TRANSITION_ID_INTERRUPT_REQUIRED, event.getId());
        }

        @Test
        void verifyInterrupted() throws Throwable {
            val context = MockRequestContext.create();

            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
            val registeredService = (BaseWebBasedRegisteredService) RegisteredServiceTestUtils.getRegisteredService();
            registeredService.setWebflowInterruptPolicy(new DefaultRegisteredServiceWebflowInterruptPolicy().setForceExecution(TriStateBoolean.TRUE));
            WebUtils.putRegisteredService(context, registeredService);

            WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
            WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());

            var event = action.execute(context);
            assertNotNull(event);
            assertNotNull(InterruptUtils.getInterruptFrom(context));
            assertNotNull(WebUtils.getPrincipalFromRequestContext(context));
            assertEquals(CasWebflowConstants.TRANSITION_ID_INTERRUPT_REQUIRED, event.getId());

            event = action.execute(context);
            assertNotNull(InterruptUtils.getInterruptFrom(context));
            assertNotNull(WebUtils.getPrincipalFromRequestContext(context));
            assertEquals(CasWebflowConstants.TRANSITION_ID_INTERRUPT_REQUIRED, event.getId());
        }

        @Test
        void verifyInterruptedAlreadyWithDifferentResponse() throws Throwable {
            val context = MockRequestContext.create();
            val authentication = CoreAuthenticationTestUtils.getAuthentication();
            WebUtils.putAuthentication(authentication, context);
            val registeredService = (BaseWebBasedRegisteredService) RegisteredServiceTestUtils.getRegisteredService();
            WebUtils.putRegisteredService(context, registeredService);
            WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
            WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());

            var event = action.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_INTERRUPT_REQUIRED, event.getId());
            val interruptResponse = InterruptResponse.interrupt().setMessage("New message");
            interruptTrackingEngine.trackInterrupt(context, interruptResponse);
            context.setRequestCookiesFromResponse();
            event = action.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_INTERRUPT_REQUIRED, event.getId());
        }
    }

    @Nested
    @SpringBootTest(classes = {
        InterruptNoneTestConfiguration.class,
        BaseInterruptFlowActionTests.SharedTestConfiguration.class
    })
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    class InterruptNoneTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_INQUIRE_INTERRUPT)
        private Action action;

        @Test
        void verifyInterruptedAlready() throws Throwable {
            val context = MockRequestContext.create();

            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(
                Map.of(InterruptTrackingEngine.AUTHENTICATION_ATTRIBUTE_FINALIZED_INTERRUPT, List.of(Boolean.TRUE))), context);
            WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService());
            WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
            WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());

            val event = action.execute(context);
            assertNotNull(event);
            assertEquals(CasWebflowConstants.TRANSITION_ID_INTERRUPT_SKIPPED, event.getId());
        }


        @Test
        void verifyInterruptFinalized() throws Throwable {
            val context = MockRequestContext.create();

            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
            WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService());
            WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
            WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
            WebUtils.putInterruptAuthenticationFlowFinalized(context);

            val event = action.execute(context);
            assertNotNull(event);
            assertEquals(CasWebflowConstants.TRANSITION_ID_INTERRUPT_SKIPPED, event.getId());
        }

        @Test
        void verifyNotInterrupted() throws Throwable {
            val context = MockRequestContext.create();

            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
            WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService());
            WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
            WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());

            val event = action.execute(context);
            assertNotNull(event);
            assertEquals(CasWebflowConstants.TRANSITION_ID_INTERRUPT_SKIPPED, event.getId());
        }

        @Test
        void verifyNotInterruptedAsFinalized() throws Throwable {
            val context = MockRequestContext.create();

            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication("casuser",
                Map.of(CasWebflowConstants.TRANSITION_ID_INTERRUPT_SKIPPED, List.of(Boolean.TRUE))), context);
            WebUtils.putRegisteredService(context, RegisteredServiceTestUtils.getRegisteredService());
            WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
            WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());

            val event = action.execute(context);
            assertNotNull(event);
            assertEquals(CasWebflowConstants.TRANSITION_ID_INTERRUPT_SKIPPED, event.getId());
        }
    }

    @TestConfiguration(value = "InterruptTestConfiguration", proxyBeanMethods = false)
    static class InterruptActiveTestConfiguration {
        @Bean
        public InterruptInquiryExecutionPlanConfigurer dummyInterruptInquirer() throws Throwable {
            val interrupt = mock(InterruptInquirer.class);
            when(interrupt.inquire(any(Authentication.class),
                any(RegisteredService.class), any(Service.class),
                any(Credential.class), any(RequestContext.class)))
                .thenReturn(InterruptResponse.interrupt());
            return plan -> plan.registerInterruptInquirer(interrupt);
        }
    }

    @TestConfiguration(value = "InterruptNoneTestConfiguration", proxyBeanMethods = false)
    static class InterruptNoneTestConfiguration {
        @Bean
        public InterruptInquiryExecutionPlanConfigurer dummyInterruptInquirer() throws Throwable {
            val interrupt = mock(InterruptInquirer.class);
            when(interrupt.inquire(any(Authentication.class),
                any(RegisteredService.class), any(Service.class),
                any(Credential.class), any(RequestContext.class)))
                .thenReturn(InterruptResponse.none());
            return plan -> plan.registerInterruptInquirer(interrupt);
        }
    }

}
