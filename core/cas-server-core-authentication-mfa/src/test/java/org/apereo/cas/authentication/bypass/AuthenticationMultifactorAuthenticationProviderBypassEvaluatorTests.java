package org.apereo.cas.authentication.bypass;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AuthenticationMultifactorAuthenticationProviderBypassEvaluatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("MFA")
public class AuthenticationMultifactorAuthenticationProviderBypassEvaluatorTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyOperation() {
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

        val eval = new DefaultChainingMultifactorAuthenticationBypassProvider();
        val bypassProps = new MultifactorAuthenticationProviderBypassProperties();
        bypassProps.setAuthenticationAttributeName("cn");
        bypassProps.setAuthenticationAttributeValue("ex.+");
        eval.addMultifactorAuthenticationProviderBypassEvaluator(
            new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(bypassProps, TestMultifactorAuthenticationProvider.ID));

        val authentication = CoreAuthenticationTestUtils.getAuthentication("casuser", Map.of("cn", List.of("example")));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val policy = new DefaultRegisteredServiceMultifactorPolicy();
        when(registeredService.getMultifactorPolicy()).thenReturn(policy);
        assertFalse(eval.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService, provider, new MockHttpServletRequest()));
    }
}
