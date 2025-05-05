package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.MultifactorAuthenticationTriggerSelectionStrategy;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.util.MockRequestContext;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DetermineMultifactorPasswordlessAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowMfaActions")
class DetermineMultifactorPasswordlessAuthenticationActionTests {
    @TestConfiguration(value = "MultifactorAuthenticationTestConfiguration", proxyBeanMethods = false)
    static class MultifactorAuthenticationTestConfiguration {
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
        "cas.authn.passwordless.accounts.simple.casuser=casuser@example.org",
        "cas.authn.passwordless.core.multifactor-authentication-activated=true"
    })
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Nested
    class WithoutMultifactorAuthenticationTrigger extends BasePasswordlessAuthenticationActionTests {

        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_DETERMINE_PASSWORDLESS_MULTIFACTOR_AUTHN)
        private Action determineMultifactorPasswordlessAuthenticationAction;

        @Test
        void verifyAction() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setFlowExecutionContext(CasWebflowConfigurer.FLOW_ID_LOGIN);

            val account = PasswordlessUserAccount.builder()
                .email("email")
                .phone("phone")
                .username("casuser")
                .name("casuser")
                .build();
            PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
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
    class WithMultifactorAuthenticationTrigger extends BasePasswordlessAuthenticationActionTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_DETERMINE_PASSWORDLESS_MULTIFACTOR_AUTHN)
        private Action determineMultifactorPasswordlessAuthenticationAction;

        @Test
        @Order(1)
        void verifyUserMfaActionDisabled() throws Throwable {
            val ctx = new StaticApplicationContext();
            ctx.refresh();
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(ctx);

            val context = MockRequestContext.create(ctx);
            context.setFlowExecutionContext(CasWebflowConfigurer.FLOW_ID_LOGIN);
            
            val account = PasswordlessUserAccount.builder()
                .email("email")
                .phone("phone")
                .username("casuser")
                .name("casuser")
                .multifactorAuthenticationEligible(TriStateBoolean.FALSE)
                .build();
            PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, determineMultifactorPasswordlessAuthenticationAction.execute(context).getId());
        }

        @Test
        @Order(2)
        void verifyUserMfaActionNoProvider() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setFlowExecutionContext(CasWebflowConfigurer.FLOW_ID_LOGIN);

            val account = PasswordlessUserAccount.builder()
                .email("email")
                .phone("phone")
                .username("casuser")
                .name("casuser")
                .multifactorAuthenticationEligible(TriStateBoolean.TRUE)
                .build();
            PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, determineMultifactorPasswordlessAuthenticationAction.execute(context).getId());
        }

        @Test
        @Order(3)
        void verifyUserMissing() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setFlowExecutionContext(CasWebflowConfigurer.FLOW_ID_LOGIN);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, determineMultifactorPasswordlessAuthenticationAction.execute(context).getId());
        }

        @Test
        @Order(4)
        void verifyUserHasNoContactInfo() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            val account = PasswordlessUserAccount.builder()
                .username("casuser")
                .build();
            PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, determineMultifactorPasswordlessAuthenticationAction.execute(context).getId());
        }

        @Test
        @Order(100)
        void verifyAction() throws Throwable {
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

            val context = MockRequestContext.create(applicationContext);
            context.setFlowExecutionContext(CasWebflowConfigurer.FLOW_ID_LOGIN);
            val account = PasswordlessUserAccount.builder()
                .email("email")
                .phone("phone")
                .username("casuser")
                .name("casuser")
                .build();
            PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
            assertEquals(TestMultifactorAuthenticationProvider.ID, determineMultifactorPasswordlessAuthenticationAction.execute(context).getId());
        }
    }

    @Import(BaseWebflowConfigurerTests.SharedTestConfiguration.class)
    @TestPropertySource(properties = {
        "cas.authn.attribute-repository.stub.attributes.groupMembership=adopters",
        "cas.authn.attribute-repository.stub.attributes.name=CAS",

        "cas.authn.passwordless.accounts.simple.casuser=casuser@helloworld.org",
        "cas.authn.passwordless.core.multifactor-authentication-activated=true",
        "cas.authn.mfa.triggers.principal.global-principal-attribute-name-triggers=groupMembership",
        "cas.authn.mfa.triggers.principal.global-principal-attribute-value-regex=adopters"
    })
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @Nested
    class WithPrincipalMultifactorAuthenticationTrigger extends BasePasswordlessAuthenticationActionTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_DETERMINE_PASSWORDLESS_MULTIFACTOR_AUTHN)
        private Action determineMultifactorPasswordlessAuthenticationAction;
        
        @Test
        void verifyAction() throws Throwable {
            TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

            val context = MockRequestContext.create(applicationContext);
            context.setFlowExecutionContext(CasWebflowConfigurer.FLOW_ID_LOGIN);

            val account = PasswordlessUserAccount.builder()
                .email("email")
                .phone("phone")
                .username("casuser")
                .name("casuser")
                .build();
            PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
            assertEquals(TestMultifactorAuthenticationProvider.ID, determineMultifactorPasswordlessAuthenticationAction.execute(context).getId());
        }
    }
}
