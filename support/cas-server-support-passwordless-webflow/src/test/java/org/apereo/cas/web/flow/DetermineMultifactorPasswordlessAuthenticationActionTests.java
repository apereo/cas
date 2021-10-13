package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.util.model.TriStateBoolean;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockFlowExecutionContext;
import org.springframework.webflow.test.MockFlowSession;
import org.springframework.webflow.test.MockRequestContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DetermineMultifactorPasswordlessAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowMfaActions")
public class DetermineMultifactorPasswordlessAuthenticationActionTests {

    @TestConfiguration("MultifactorAuthenticationTestConfiguration")
    public static class MultifactorAuthenticationTestConfiguration {
        @Bean
        public MultifactorAuthenticationTriggerSelectionStrategy defaultMultifactorTriggerSelectionStrategy() {
            return new DefaultMultifactorAuthenticationTriggerSelectionStrategy(List.of());
        }
    }

    @Import({
        DetermineMultifactorPasswordlessAuthenticationActionTests.MultifactorAuthenticationTestConfiguration.class,
        BaseWebflowConfigurerTests.SharedTestConfiguration.class
    })
    @TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "cas.authn.passwordless.accounts.simple.casuser=casuser@example.org",
        "cas.authn.passwordless.core.multifactor-authentication-activated=true"
    })
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class WithoutMultifactorAuthenticationTrigger extends BasePasswordlessAuthenticationActionTests {

        @Autowired
        @Qualifier("determineMultifactorPasswordlessAuthenticationAction")
        private Action determineMultifactorPasswordlessAuthenticationAction;

        @Test
        public void verifyAction() throws Exception {
            val exec = new MockFlowExecutionContext(new MockFlowSession(new Flow(CasWebflowConfigurer.FLOW_ID_LOGIN)));
            val context = new MockRequestContext(exec);
            val request = new MockHttpServletRequest();
            val account = PasswordlessUserAccount.builder()
                .email("email")
                .phone("phone")
                .username("casuser")
                .name("casuser")
                .build();
            WebUtils.putPasswordlessAuthenticationAccount(context, account);
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, determineMultifactorPasswordlessAuthenticationAction.execute(context).getId());
        }
    }

    @Import(BaseWebflowConfigurerTests.SharedTestConfiguration.class)
    @TestPropertySource(properties = {
        "cas.authn.passwordless.accounts.simple.casuser=casuser@example.org",
        "cas.authn.passwordless.core.multifactor-authentication-activated=true",
        "cas.authn.mfa.triggers.global.global-provider-id=" + TestMultifactorAuthenticationProvider.ID
    })
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class WithMultifactorAuthenticationTrigger extends BasePasswordlessAuthenticationActionTests {
        @Autowired
        @Qualifier("determineMultifactorPasswordlessAuthenticationAction")
        private Action determineMultifactorPasswordlessAuthenticationAction;

        @Test
        @Order(1)
        public void verifyUserMfaActionDisabled() throws Exception {
            val applicationContext = new StaticApplicationContext();
            applicationContext.refresh();
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

            val exec = new MockFlowExecutionContext(new MockFlowSession(new Flow(CasWebflowConfigurer.FLOW_ID_LOGIN)));
            val context = new MockRequestContext(exec);
            val request = new MockHttpServletRequest();
            val account = PasswordlessUserAccount.builder()
                .email("email")
                .phone("phone")
                .username("casuser")
                .name("casuser")
                .multifactorAuthenticationEligible(TriStateBoolean.FALSE)
                .build();
            WebUtils.putPasswordlessAuthenticationAccount(context, account);
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, determineMultifactorPasswordlessAuthenticationAction.execute(context).getId());
        }

        @Test
        @Order(2)
        public void verifyUserMfaActionNoProvider() throws Exception {
            val exec = new MockFlowExecutionContext(new MockFlowSession(new Flow(CasWebflowConfigurer.FLOW_ID_LOGIN)));
            val context = new MockRequestContext(exec);
            val request = new MockHttpServletRequest();
            val account = PasswordlessUserAccount.builder()
                .email("email")
                .phone("phone")
                .username("casuser")
                .name("casuser")
                .multifactorAuthenticationEligible(TriStateBoolean.TRUE)
                .build();
            WebUtils.putPasswordlessAuthenticationAccount(context, account);
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, determineMultifactorPasswordlessAuthenticationAction.execute(context).getId());
        }

        @Test
        @Order(3)
        public void verifyUserMissing() throws Exception {
            val exec = new MockFlowExecutionContext(new MockFlowSession(new Flow(CasWebflowConfigurer.FLOW_ID_LOGIN)));
            val context = new MockRequestContext(exec);
            val request = new MockHttpServletRequest();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, determineMultifactorPasswordlessAuthenticationAction.execute(context).getId());
        }

        @Test
        @Order(100)
        public void verifyAction() throws Exception {
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

            val exec = new MockFlowExecutionContext(new MockFlowSession(new Flow(CasWebflowConfigurer.FLOW_ID_LOGIN)));
            val context = new MockRequestContext(exec);
            val request = new MockHttpServletRequest();
            val account = PasswordlessUserAccount.builder()
                .email("email")
                .phone("phone")
                .username("casuser")
                .name("casuser")
                .build();
            WebUtils.putPasswordlessAuthenticationAccount(context, account);
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            assertEquals(TestMultifactorAuthenticationProvider.ID, determineMultifactorPasswordlessAuthenticationAction.execute(context).getId());
        }
    }
}
