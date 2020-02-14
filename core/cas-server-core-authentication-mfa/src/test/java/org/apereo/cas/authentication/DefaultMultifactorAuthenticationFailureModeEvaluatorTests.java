package org.apereo.cas.authentication;

import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicyFailureModes;

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
@SpringBootTest(classes = RefreshAutoConfiguration.class, properties = "cas.authn.mfa.global-failure-mode=phantom")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("MFA")
public class DefaultMultifactorAuthenticationFailureModeEvaluatorTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyOperations() {
        executeEvaluation(
            RegisteredServiceMultifactorPolicyFailureModes.UNDEFINED,
            RegisteredServiceMultifactorPolicyFailureModes.UNDEFINED,
            RegisteredServiceMultifactorPolicyFailureModes.PHANTOM);

        executeEvaluation(
            RegisteredServiceMultifactorPolicyFailureModes.OPEN,
            RegisteredServiceMultifactorPolicyFailureModes.CLOSED,
            RegisteredServiceMultifactorPolicyFailureModes.CLOSED);

        executeEvaluation(
            RegisteredServiceMultifactorPolicyFailureModes.NONE,
            RegisteredServiceMultifactorPolicyFailureModes.UNDEFINED,
            RegisteredServiceMultifactorPolicyFailureModes.NONE);
    }

    protected void executeEvaluation(final RegisteredServiceMultifactorPolicyFailureModes providerMode,
                                     final RegisteredServiceMultifactorPolicyFailureModes serviceMode,
                                     final RegisteredServiceMultifactorPolicyFailureModes expected) {
        val eval = new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties);

        val provider = new TestMultifactorAuthenticationProvider();
        provider.setFailureMode(providerMode.name());
        val service = CoreAuthenticationTestUtils.getRegisteredService();
        val policy = new DefaultRegisteredServiceMultifactorPolicy();
        policy.setFailureMode(serviceMode);
        when(service.getMultifactorPolicy()).thenReturn(policy);

        val result = eval.evaluate(service, provider);
        assertEquals(expected, result);
    }
}
