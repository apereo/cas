package org.apereo.cas.pm.web.flow.actions;

import module java.base;
import org.apereo.cas.authentication.DefaultAuthenticationResultBuilder;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.config.CasSimpleMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.pm.PasswordManagementQuery;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowUtils;
import org.apereo.cas.pm.web.flow.PasswordResetRequest;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InitPasswordResetActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Mail")
@EnabledIfListeningOnPort(port = 25000)
class InitPasswordResetActionTests extends BasePasswordManagementActionTests {

    @Nested
    class DefaultTests extends BasePasswordManagementActionTests {
        @Test
        void verifyAction() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setRemoteAddr("1.2.3.4");
            context.setLocalAddr("1.2.3.4");
            context.setClientInfo();

            val query = PasswordManagementQuery.builder().username(UUID.randomUUID().toString()).build();
            val token = passwordManagementService.createToken(query);

            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, initPasswordResetAction.execute(context).getId());

            context.getFlowScope().put(PasswordManagementService.PARAMETER_TOKEN, token);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, initPasswordResetAction.execute(context).getId());
            val credential = WebUtils.getCredential(context, UsernamePasswordCredential.class);
            assertNotNull(credential);
            assertEquals(query.getUsername(), credential.getUsername());
        }

        @Test
        void verifyActionUserlessToken() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val token = passwordManagementService.createToken(PasswordManagementQuery.builder().build());
            context.getFlowScope().put("token", token);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, initPasswordResetAction.execute(context).getId());
        }

        @Test
        void verifyActionUserWithResetRequest() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val request = PasswordResetRequest.builder().username(UUID.randomUUID().toString()).build();
            PasswordManagementWebflowUtils.putPasswordResetRequest(context, request);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, initPasswordResetAction.execute(context).getId());
        }
    }

    @Nested
    @Import(MfaHasNoDevicesTests.MfaHasNoDevicesTestConfiguration.class)
    @TestPropertySource(properties = "cas.authn.pm.reset.multifactor-authentication-enabled=true")
    class MfaHasNoDevicesTests extends BasePasswordManagementActionTests {
        @Test
        void verifyAction() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setRemoteAddr("1.2.3.4");
            context.setLocalAddr("1.2.3.4");
            context.setClientInfo();

            val query = PasswordManagementQuery.builder().username("user-without-devices").build();
            val token = passwordManagementService.createToken(query);
            context.getFlowScope().put(PasswordManagementService.PARAMETER_TOKEN, token);
            val event = initPasswordResetAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, event.getId());
        }

        @TestConfiguration(value = "MfaHasNoDevicesTestConfiguration", proxyBeanMethods = false)
        static class MfaHasNoDevicesTestConfiguration {
            @Bean
            public MultifactorAuthenticationProvider dummyProvider() {
                return new TestMultifactorAuthenticationProvider();
            }
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.pm.reset.multifactor-authentication-enabled=true")
    @ImportAutoConfiguration(CasSimpleMultifactorAuthenticationAutoConfiguration.class)
    class MfaRequiredTests extends BasePasswordManagementActionTests {
        @Autowired
        @Qualifier("casSimpleMultifactorAuthenticationProvider")
        private MultifactorAuthenticationProvider multifactorAuthenticationProvider;

        @Autowired
        @Qualifier(PrincipalElectionStrategy.BEAN_NAME)
        private PrincipalElectionStrategy principalElectionStrategy;

        @Test
        void verifyAction() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setRemoteAddr("1.2.3.4");
            context.setLocalAddr("1.2.3.4");
            context.setClientInfo();

            val query = PasswordManagementQuery.builder().username(UUID.randomUUID().toString()).build();
            val token = passwordManagementService.createToken(query);
            context.getFlowScope().put(PasswordManagementService.PARAMETER_TOKEN, token);
            val event = initPasswordResetAction.execute(context);
            assertEquals(multifactorAuthenticationProvider.getId(), event.getId());
            assertNotNull(WebUtils.getAuthentication(context));
            assertNotNull(WebUtils.getAuthenticationResultBuilder(context));
            assertEquals(CasWebflowConstants.TRANSITION_ID_RESUME_RESET_PASSWORD, WebUtils.getTargetTransition(context));
            assertEquals(multifactorAuthenticationProvider.getId(), MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationProvider(context));
        }

        @Test
        void verifyMfaHandledAction() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setRemoteAddr("1.2.3.4");
            context.setLocalAddr("1.2.3.4");
            context.setClientInfo();

            val query = PasswordManagementQuery.builder().username(UUID.randomUUID().toString()).build();
            val token = passwordManagementService.createToken(query);
            context.getFlowScope().put(PasswordManagementService.PARAMETER_TOKEN, token);

            val resultBuilder = new DefaultAuthenticationResultBuilder(principalElectionStrategy)
                .collect(RegisteredServiceTestUtils.getAuthentication(query.getUsername(),
                    Map.of(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute(),
                        List.of(multifactorAuthenticationProvider.getId()))));

            WebUtils.putAuthenticationResultBuilder(resultBuilder, context);
            val event = initPasswordResetAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, event.getId());
            val credential = WebUtils.getCredential(context, UsernamePasswordCredential.class);
            assertNotNull(credential);
            assertEquals(query.getUsername(), credential.getUsername());
        }
    }
}
