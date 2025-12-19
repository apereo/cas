package org.apereo.cas.authentication.bypass;

import module java.base;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

/**
 * This is {@link AuthenticationMultifactorAuthenticationProviderBypassEvaluatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("MFATrigger")
class AuthenticationMultifactorAuthenticationProviderBypassEvaluatorTests {
    private ConfigurableApplicationContext applicationContext;

    @BeforeEach
    void beforeEach() {
        applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            MultifactorAuthenticationPrincipalResolver.identical(), UUID.randomUUID().toString());
    }

    @ParameterizedTest
    @MethodSource("getTestAuthAttributes")
    void verifyOperationByAuthAttribute(final String attributeValuePattern, final List<Object> attributeValue) {
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val eval = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
        val bypassProps = new MultifactorAuthenticationProviderBypassProperties();
        bypassProps.setAuthenticationAttributeName("cn");
        bypassProps.setAuthenticationAttributeValue(attributeValuePattern);
        eval.addMultifactorAuthenticationProviderBypassEvaluator(new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(
            bypassProps, TestMultifactorAuthenticationProvider.ID, applicationContext));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString(), Map.of("cn", attributeValue));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val policy = new DefaultRegisteredServiceMultifactorPolicy();
        when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);
        assertFalse(eval.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService,
            provider, new MockHttpServletRequest(), CoreAuthenticationTestUtils.getService()));
    }

    @ParameterizedTest
    @MethodSource("getTestAuthHandlerNames")
    void verifyOperationByAuthAHandlerName(final String handlerNamePattern, final List<Object> handlerNames) {
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val eval = new DefaultChainingMultifactorAuthenticationBypassProvider(applicationContext);
        val bypassProps = new MultifactorAuthenticationProviderBypassProperties();
        bypassProps.setAuthenticationHandlerName(handlerNamePattern);
        eval.addMultifactorAuthenticationProviderBypassEvaluator(new AuthenticationMultifactorAuthenticationProviderBypassEvaluator(
            bypassProps, TestMultifactorAuthenticationProvider.ID, applicationContext));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString(),
            Map.of(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS, handlerNames));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val policy = new DefaultRegisteredServiceMultifactorPolicy();
        when(registeredService.getMultifactorAuthenticationPolicy()).thenReturn(policy);
        assertFalse(eval.shouldMultifactorAuthenticationProviderExecute(authentication, registeredService,
            provider, new MockHttpServletRequest(), CoreAuthenticationTestUtils.getService()));
    }

    public static Stream<Arguments> getTestAuthHandlerNames() {
        return Stream.of(
            arguments("^Static.*", List.of("StaticHandler")),
            arguments("^Unknown.*,-\\d{4}-", List.of("StaticHandler", "FancyHandler", "MFA-1984-Handler"))
        );
    }

    public static Stream<Arguments> getTestAuthAttributes() {
        return Stream.of(
            arguments("ex.+", List.of("example")),
            arguments("ex.+", List.of("abc", "efg", "external")),
            arguments("^xyz,\\d{4}\\w+", List.of("abc", "1984T"))
        );
    }
}
