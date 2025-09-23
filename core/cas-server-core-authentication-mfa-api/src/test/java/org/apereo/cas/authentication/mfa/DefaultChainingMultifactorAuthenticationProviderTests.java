package org.apereo.cas.authentication.mfa;

import org.apereo.cas.authentication.DefaultChainingMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.bypass.HttpRequestMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = AopAutoConfiguration.class)
@Tag("MFA")
@ExtendWith(CasTestExtension.class)
class DefaultChainingMultifactorAuthenticationProviderTests {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() {
        val props = new MultifactorAuthenticationProviderBypassProperties();
        props.setHttpRequestHeaders("headerbypass");
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        provider.setBypassEvaluator(new HttpRequestMultifactorAuthenticationProviderBypassEvaluator(props, provider.getId(), applicationContext));
        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().getCore().setGlobalFailureMode(BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.OPEN);
        val failureEvaluator = new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties);
        val chain = new DefaultChainingMultifactorAuthenticationProvider(applicationContext, failureEvaluator);
        chain.addMultifactorAuthenticationProviders(provider);
        assertNotNull(chain.getBypassEvaluator());
        assertNotNull(chain.getId());
        assertNotNull(chain.getFriendlyName());
        assertEquals(BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes.NONE, chain.getFailureMode());

        assertFalse(chain.getMultifactorAuthenticationProviders().isEmpty());
        assertEquals("TestMfaProvider", chain.getDeviceManager().getSource().getFirst());
        val service = MultifactorAuthenticationTestUtils.getRegisteredService();
        assertTrue(chain.isAvailable(service));
        assertTrue(chain.matches(provider.getId()));
        val registeredDevices = chain.getDeviceManager().findRegisteredDevices(MultifactorAuthenticationTestUtils.getPrincipal("casuser"));
        assertEquals(1, registeredDevices.size());
    }
}
