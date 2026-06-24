package org.apereo.cas.authentication.principal;

import module java.base;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.credentials.TokenCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientAuthenticationPostProcessorTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("Delegation")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    DelegatedClientAuthenticationPostProcessorTests.TestMultifactorTestConfiguration.class,
    BaseDelegatedAuthenticationTests.SharedTestConfiguration.class
}, properties = "cas.authn.mfa.triggers.global.global-provider-id=mfa-dummy")
class DelegatedClientAuthenticationPostProcessorTests {
    @Autowired
    @Qualifier("clientAuthenticationPostProcessor")
    private AuthenticationPostProcessor clientAuthenticationPostProcessor;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    void verifyOperation() throws Throwable {
        MockRequestContext.create(applicationContext);
        val credentials = new TokenCredentials(UUID.randomUUID().toString());
        val clientCredential = new ClientCredential(credentials, "FakeClient");
        assertTrue(clientAuthenticationPostProcessor.supports(clientCredential));

        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
            .newTransaction(RegisteredServiceTestUtils.getService("service"), clientCredential);
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            Map.of("http://schemas.microsoft.com/claims/authnmethodsreferences",
                List.of("password", "http://schemas.microsoft.com/claims/multipleauthn")));

        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder(principal);
        clientAuthenticationPostProcessor.process(builder, transaction);
        assertTrue(builder.hasAttribute(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute()));
    }

    @Test
    void verifyOperationForMfaAttribute() throws Throwable {
        MockRequestContext.create(applicationContext);
        val credentials = new TokenCredentials(UUID.randomUUID().toString());
        val clientCredential = new ClientCredential(credentials, "FakeClient");
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
            .newTransaction(RegisteredServiceTestUtils.getService("service"), clientCredential);
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            Map.of("amr", List.of("password", "mfa")));

        val builder = CoreAuthenticationTestUtils.getAuthenticationBuilder(principal);
        clientAuthenticationPostProcessor.process(builder, transaction);
        assertTrue(builder.hasAttribute(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute()));
    }

    @TestConfiguration(value = "TestMultifactorTestConfiguration", proxyBeanMethods = false)
    static class TestMultifactorTestConfiguration {
        @Autowired
        private CasConfigurationProperties casProperties;

        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Bean
        public MultifactorAuthenticationProvider dummyProvider() {
            val failureEvaluator = new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties);
            val chain = new DefaultChainingMultifactorAuthenticationProvider(applicationContext, failureEvaluator);
            chain.addMultifactorAuthenticationProviders(new TestMultifactorAuthenticationProvider());
            return chain;
        }
    }
}
