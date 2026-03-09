package org.apereo.cas.authentication.bypass;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RegisteredServiceMultifactorAuthenticationProviderBypassEvaluatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("MFATrigger")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
class RegisteredServiceMultifactorAuthenticationProviderBypassEvaluatorTests {
    @Test
    void verifyOperation() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val eval = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
        eval.addMultifactorAuthenticationProviderBypassEvaluator(
            new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(TestMultifactorAuthenticationProvider.ID, applicationContext));
        val principal = CoreAuthenticationTestUtils.getPrincipal(Map.of("cn", List.of("example")));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val policy = new DefaultRegisteredServiceMultifactorPolicy();
        policy.setBypassEnabled(true);
        when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);
        assertFalse(eval.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService,
            provider, new MockHttpServletRequest(), CoreAuthenticationTestUtils.getService()));
    }

    @Test
    void verifyOperationByIpAddress() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val eval = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
        eval.addMultifactorAuthenticationProviderBypassEvaluator(
            new RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(TestMultifactorAuthenticationProvider.ID, applicationContext));
        val principal = CoreAuthenticationTestUtils.getPrincipal(Map.of("cn", List.of("example")));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(principal);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val policy = new DefaultRegisteredServiceMultifactorPolicy();
        policy.setBypassEnabled(false);
        policy.setBypassForRequestIpAddress("^172.+");
        when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("172.4.5.6");
        assertFalse(eval.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService,
            provider, request, CoreAuthenticationTestUtils.getService()));
    }
}
