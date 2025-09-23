package org.apereo.cas.web.flow.delegation;

import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.config.CasDelegatedAuthenticationCasAutoConfiguration;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.BasePasswordlessAuthenticationActionTests;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.PasswordlessWebflowUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.pac4j.core.util.Pac4jConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PasswordlessDetermineDelegatedAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowAuthenticationActions")
@Execution(ExecutionMode.SAME_THREAD)
class PasswordlessDetermineDelegatedAuthenticationActionTests {
    @Nested
    @Import(BaseWebflowConfigurerTests.SharedTestConfiguration.class)
    @TestPropertySource(properties = {
        "cas.authn.passwordless.accounts.simple.casuser=casuser@example.org",
        "cas.authn.passwordless.core.delegated-authentication-activated=true",
        "cas.authn.passwordless.core.delegated-authentication-selector-script.location=classpath:/DelegatedAuthenticationSelectorScript.groovy"
    })
    class WithoutClients extends BasePasswordlessAuthenticationActionTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_DETERMINE_PASSWORDLESS_DELEGATED_AUTHN)
        private Action determineDelegatedAuthenticationAction;

        @Test
        void verifyNoClients() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setFlowExecutionContext(CasWebflowConfigurer.FLOW_ID_LOGIN);

            val account = PasswordlessUserAccount.builder()
                .email("email")
                .phone("phone")
                .username("casuser")
                .name("casuser")
                .build();
            PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, determineDelegatedAuthenticationAction.execute(context).getId());
        }
    }

    @Nested
    @ImportAutoConfiguration(CasDelegatedAuthenticationCasAutoConfiguration.class)
    @Import(BaseWebflowConfigurerTests.SharedTestConfiguration.class)
    @TestPropertySource(properties = {
        "cas.authn.pac4j.cas[0].login-url=https://casserver.herokuapp.com/cas/login",
        "cas.authn.pac4j.cas[0].protocol=CAS30",
        "cas.authn.passwordless.accounts.simple.casuser=casuser@example.org",
        "cas.authn.passwordless.core.delegated-authentication-activated=true",
        "cas.authn.passwordless.core.delegated-authentication-selector-script.location=classpath:/DelegatedAuthenticationSelectorScript.groovy"
    })
    class WithClients extends BasePasswordlessAuthenticationActionTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_DETERMINE_PASSWORDLESS_DELEGATED_AUTHN)
        private Action determineDelegatedAuthenticationAction;

        @Test
        void verifyNoAcct() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setFlowExecutionContext(CasWebflowConfigurer.FLOW_ID_LOGIN);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, determineDelegatedAuthenticationAction.execute(context).getId());
        }

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
            assertEquals(CasWebflowConstants.TRANSITION_ID_PROMPT, determineDelegatedAuthenticationAction.execute(context).getId());
        }

        @Test
        void verifyCantDetermineIdP() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setFlowExecutionContext(CasWebflowConfigurer.FLOW_ID_LOGIN);

            val account = PasswordlessUserAccount.builder()
                .email("email")
                .phone("phone")
                .username("unknown")
                .name("unknown")
                .build();
            PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);

            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, determineDelegatedAuthenticationAction.execute(context).getId());
        }

        @Test
        void verifyActionByUser() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setFlowExecutionContext(CasWebflowConfigurer.FLOW_ID_LOGIN);

            val account = PasswordlessUserAccount.builder()
                .email("email")
                .phone("phone")
                .delegatedAuthenticationEligible(TriStateBoolean.TRUE)
                .username("casuser")
                .name("casuser")
                .build();
            PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
            assertEquals(CasWebflowConstants.TRANSITION_ID_PROMPT, determineDelegatedAuthenticationAction.execute(context).getId());
            assertNotNull(context.getHttpServletRequest().getAttribute(Pac4jConstants.DEFAULT_CLIENT_NAME_PARAMETER));
        }

        @Test
        void verifyActionByUserDisallowed() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setFlowExecutionContext(CasWebflowConfigurer.FLOW_ID_LOGIN);

            val account = PasswordlessUserAccount
                .builder()
                .email("email")
                .phone("phone")
                .delegatedAuthenticationEligible(TriStateBoolean.TRUE)
                .allowedDelegatedClients(List.of("UnknownClient"))
                .username("casuser")
                .name("casuser")
                .build();
            PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, determineDelegatedAuthenticationAction.execute(context).getId());
        }

        @Test
        void verifyAuthInactive() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setFlowExecutionContext(CasWebflowConfigurer.FLOW_ID_LOGIN);

            val account = PasswordlessUserAccount.builder()
                .email("email")
                .phone("phone")
                .username("casuser")
                .name("casuser")
                .delegatedAuthenticationEligible(TriStateBoolean.FALSE)
                .build();
            PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, determineDelegatedAuthenticationAction.execute(context).getId());
        }
    }
}
