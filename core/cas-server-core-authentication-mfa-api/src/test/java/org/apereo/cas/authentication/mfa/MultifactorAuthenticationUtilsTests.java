package org.apereo.cas.authentication.mfa;

import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.DefaultTransitionCriteria;
import org.springframework.webflow.test.MockRequestContext;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MultifactorAuthenticationUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@DirtiesContext
@SpringBootTest(classes = AopAutoConfiguration.class)
@Tag("MFA")
public class MultifactorAuthenticationUtilsTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyResolveByAttribute() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        ApplicationContextProvider.holdApplicationContext(applicationContext);
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        assertTrue(MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderFromApplicationContext(provider.getId()).isPresent());

        val targetResolver = new DefaultTargetStateResolver(TestMultifactorAuthenticationProvider.ID);
        val transition = new Transition(new DefaultTransitionCriteria(
            new LiteralExpression(TestMultifactorAuthenticationProvider.ID)), targetResolver);
        context.getRootFlow().getGlobalTransitionSet().add(transition);

        val result = MultifactorAuthenticationUtils.resolveEventViaMultivaluedAttribute(MultifactorAuthenticationTestUtils.getPrincipal("casuser"),
            List.of("mfa-value"), MultifactorAuthenticationTestUtils.getRegisteredService(),
            Optional.of(context), provider, (s, mfaProvider) -> RegexUtils.find("mfa-.+", s));
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void verifyMultivaluedAttrs() {
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        var result = MultifactorAuthenticationUtils.resolveEventViaMultivaluedAttribute(MultifactorAuthenticationTestUtils.getPrincipal("casuser"),
            List.of("some-value"), MultifactorAuthenticationTestUtils.getRegisteredService(), Optional.of(context), provider,
            (s, mfaProvider) -> {
                throw new RuntimeException("Bad Predicate");
            });
        assertNotNull(result);
        assertTrue(result.isEmpty());

        result = MultifactorAuthenticationUtils.resolveEventViaMultivaluedAttribute(MultifactorAuthenticationTestUtils.getPrincipal("casuser"),
            "some-value", MultifactorAuthenticationTestUtils.getRegisteredService(), Optional.of(context), provider,
            (s, mfaProvider) -> {
                throw new RuntimeException("Bad Predicate");
            });
        assertNull(result);
    }

    @Test
    public void verifyProviderByService() {
        ApplicationContextProvider.holdApplicationContext(applicationContext);
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        assertTrue(MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderFromApplicationContext(provider.getId()).isPresent());

        val registeredService = MultifactorAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getMultifactorPolicy().getMultifactorAuthenticationProviders()).thenReturn(Set.of("mfa-other"));
        var result = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderForService(registeredService);
        assertNotNull(result);
        assertTrue(result.isEmpty());

        when(registeredService.getMultifactorPolicy().getMultifactorAuthenticationProviders()).thenReturn(Set.of(provider.getId()));
        result = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderForService(registeredService);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}
