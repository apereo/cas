package org.apereo.cas.otp.web.flow;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.token.BaseOneTimeTokenRepositoryTests;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.webflow.execution.Action;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OneTimeTokenAccountSaveRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowMfaActions")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    OneTimeTokenAccountSaveRegistrationActionTests.OneTimeTokenAccountTestConfiguration.class,
    BaseOneTimeTokenRepositoryTests.SharedTestConfiguration.class
})
@Getter
class OneTimeTokenAccountSaveRegistrationActionTests extends BaseOneTimeTokenRepositoryTests {
    @Autowired
    @Qualifier(OneTimeTokenRepository.BEAN_NAME)
    private OneTimeTokenRepository repository;

    @Autowired
    @Qualifier("oneTimeTokenAccountSaveRegistrationAction")
    private Action oneTimeTokenAccountSaveRegistrationAction;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyCreateAccount() throws Throwable {
        val account = buildAccount();
        val context = MockRequestContext.create(applicationContext);
        context.setParameter(OneTimeTokenAccountSaveRegistrationAction.REQUEST_PARAMETER_ACCOUNT_NAME, "ExampleAccount");
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(account.getUsername()), context);
        context.getFlowScope().put(OneTimeTokenAccountCreateRegistrationAction.FLOW_SCOPE_ATTR_ACCOUNT, account);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, oneTimeTokenAccountSaveRegistrationAction.execute(context).getId());
    }

    @Test
    void verifyRegistrationDisabled() throws Throwable {
        val account = buildAccount();
        val context = MockRequestContext.create(applicationContext);
        context.setParameter(OneTimeTokenAccountSaveRegistrationAction.REQUEST_PARAMETER_ACCOUNT_NAME, "ExampleAccount");
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(account.getUsername()), context);
        context.getFlowScope().put(OneTimeTokenAccountCreateRegistrationAction.FLOW_SCOPE_ATTR_ACCOUNT, account);
        MultifactorAuthenticationWebflowUtils.putMultifactorDeviceRegistrationEnabled(context, false);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, oneTimeTokenAccountSaveRegistrationAction.execute(context).getId());
    }

    @Test
    void verifyMissingAccount() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, oneTimeTokenAccountSaveRegistrationAction.execute(context).getId());
    }
    
    private static OneTimeTokenAccount buildAccount() {
        return OneTimeTokenAccount.builder()
            .username(UUID.randomUUID().toString())
            .secretKey(UUID.randomUUID().toString())
            .validationCode(123456)
            .scratchCodes(List.of())
            .name(UUID.randomUUID().toString())
            .build();
    }

    @TestConfiguration(value = "OneTimeTokenAccountTestConfiguration", proxyBeanMethods = false)
    public static class OneTimeTokenAccountTestConfiguration {
        @Bean
        public Action oneTimeTokenAccountSaveRegistrationAction(
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            @Qualifier("oneTimeTokenAccountRegistry")
            final OneTimeTokenCredentialRepository oneTimeTokenAccountRegistry,
            final CasConfigurationProperties casProperties) {
            return new OneTimeTokenAccountSaveRegistrationAction(oneTimeTokenAccountRegistry, casProperties, tenantExtractor);
        }

        @Bean
        public OneTimeTokenCredentialRepository oneTimeTokenAccountRegistry() {
            return mock(OneTimeTokenCredentialRepository.class);
        }
    }
}
