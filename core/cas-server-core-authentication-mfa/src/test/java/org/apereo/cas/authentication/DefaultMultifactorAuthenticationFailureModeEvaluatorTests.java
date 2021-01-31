package org.apereo.cas.authentication;

import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultMultifactorAuthenticationFailureModeEvaluatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class, properties = "cas.authn.mfa.core.global-failure-mode=PHANTOM")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("MFA")
public class DefaultMultifactorAuthenticationFailureModeEvaluatorTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyOperations() {
        executeEvaluation(
            BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.UNDEFINED,
            BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.UNDEFINED,
            BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.PHANTOM);

        executeEvaluation(
            BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.OPEN,
            BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.CLOSED,
            BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.CLOSED);

        executeEvaluation(
            BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.NONE,
            BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.UNDEFINED,
            BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.NONE);
    }

    protected void executeEvaluation(final BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes providerMode,
                                     final BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes serviceMode,
                                     final BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes expected) {
        val eval = new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties);

        val provider = new TestMultifactorAuthenticationProvider();
        provider.setFailureMode(providerMode);
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val policy = new DefaultRegisteredServiceMultifactorPolicy();
        policy.setFailureMode(serviceMode);
        when(service.getMultifactorPolicy()).thenReturn(policy);

        val result = eval.evaluate(service, provider);
        assertEquals(expected, result);
    }
}
