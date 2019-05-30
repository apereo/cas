package org.apereo.cas.authentication.mfa;

import org.apereo.cas.authentication.DefaultChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.bypass.HttpRequestMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicyFailureModes;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultChainingMultifactorAuthenticationProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = AopAutoConfiguration.class)
public class DefaultChainingMultifactorAuthenticationProviderTests {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyOperation() {
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setHttpRequestHeaders("headerbypass");
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        provider.setBypassEvaluator(new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, provider.getId()));

        val p = new DefaultChainingMultifactorAuthenticationProvider();
        p.addMultifactorAuthenticationProviders(provider);
        assertNotNull(p.getBypassEvaluator());
        assertNotNull(p.getId());
        assertNotNull(p.getFriendlyName());
        assertEquals(RegisteredServiceMultifactorPolicyFailureModes.NONE, p.getFailureMode());

        assertFalse(p.getMultifactorAuthenticationProviders().isEmpty());
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertTrue(p.isAvailable(service));
        assertTrue(p.matches(provider.getId()));
    }
}
