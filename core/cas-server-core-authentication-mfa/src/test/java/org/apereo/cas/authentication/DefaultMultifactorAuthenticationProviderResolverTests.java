package org.apereo.cas.authentication;

import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.mfa.trigger.AuthenticationAttributeMultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.mfa.trigger.PrincipalAttributeMultifactorAuthenticationTrigger;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.DefaultTransitionCriteria;
import org.springframework.webflow.test.MockRequestContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultMultifactorAuthenticationProviderResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */

@Tag("MFA")
public class DefaultMultifactorAuthenticationProviderResolverTests {
    @Test
    public void verifyMultipleProvidersWithPrincipalAttributes() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().setGlobalPrincipalAttributeNameTriggers("mfa-principal");

        val resolver = new DefaultMultifactorAuthenticationProviderResolver();
        val trigger = new PrincipalAttributeMultifactorAuthenticationTrigger(casProperties, resolver, applicationContext);
        assertProviderResolutionFromManyProviders(trigger, applicationContext, true);
    }

    @Test
    public void verifyInvalidPrincipalAttributes() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().setGlobalPrincipalAttributeNameTriggers("does-not-exist");
        val resolver = new DefaultMultifactorAuthenticationProviderResolver();
        val trigger = new PrincipalAttributeMultifactorAuthenticationTrigger(casProperties, resolver, applicationContext);
        assertProviderResolutionFromManyProviders(trigger, applicationContext, false);
    }


    @Test
    public void verifyMultipleProvidersWithAuthenticationAttributes() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val casProperties = new CasConfigurationProperties();
        casProperties.getAuthn().getMfa().setGlobalAuthenticationAttributeNameTriggers("mfa-authn");

        val resolver = new DefaultMultifactorAuthenticationProviderResolver();
        val trigger = new AuthenticationAttributeMultifactorAuthenticationTrigger(casProperties, resolver, applicationContext);
        assertProviderResolutionFromManyProviders(trigger, applicationContext, true);
    }


    @Test
    public void verifyResolutionByAuthenticationAttribute() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val provider = registerProviderInApplicationContext(applicationContext, context, new TestMultifactorAuthenticationProvider());
        val resolver = new DefaultMultifactorAuthenticationProviderResolver();

        val authentication = CoreAuthenticationTestUtils.getAuthentication("casuser",
            CollectionUtils.wrap("authlevel", List.of(provider.getId())));
        val results = resolver.resolveEventViaAuthenticationAttribute(authentication,
            List.of("authlevel"), CoreAuthenticationTestUtils.getRegisteredService(),
            Optional.of(context), List.of(provider), (input, mfaProvider) -> input.equalsIgnoreCase(provider.getId()));
        assertNotNull(results);
        assertEquals(provider.getId(), results.iterator().next().getId());
    }

    @Test
    public void verifyResolutionByPrincipalAttribute() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val provider = registerProviderInApplicationContext(applicationContext, context, new TestMultifactorAuthenticationProvider());
        val resolver = new DefaultMultifactorAuthenticationProviderResolver();

        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("authlevel", List.of(provider.getId())));
        var results = resolver.resolveEventViaPrincipalAttribute(principal,
            List.of("authlevel"), CoreAuthenticationTestUtils.getRegisteredService(),
            Optional.of(context), List.of(provider), (input, mfaProvider) -> input.equalsIgnoreCase(provider.getId()));
        assertNotNull(results);
        assertEquals(provider.getId(), results.iterator().next().getId());

        results = resolver.resolveEventViaPrincipalAttribute(principal,
            List.of("authlevel"), CoreAuthenticationTestUtils.getRegisteredService(),
            Optional.of(context), List.of(), (input, mfaProvider) -> input.equalsIgnoreCase(provider.getId()));
        assertNull(results);

        results = resolver.resolveEventViaPrincipalAttribute(principal,
            List.of(), CoreAuthenticationTestUtils.getRegisteredService(),
            Optional.of(context), List.of(), (input, mfaProvider) -> input.equalsIgnoreCase(provider.getId()));
        assertNull(results);
    }

    @Test
    public void verifyNoProvider() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        val resolver = new DefaultMultifactorAuthenticationProviderResolver();
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val results = resolver.resolveEventViaAttribute(principal,
            Map.of("authlevel", List.of("strong")),
            List.of(), CoreAuthenticationTestUtils.getRegisteredService(),
            Optional.of(context), List.of(), (s, mfaProvider) -> false);
        assertNull(results);
    }

    @Test
    public void verifyNoMatch() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val resolver = new DefaultMultifactorAuthenticationProviderResolver();
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val results = resolver.resolveEventViaAttribute(principal,
            Map.of("authlevel", List.of("strong")),
            List.of(), CoreAuthenticationTestUtils.getRegisteredService(),
            Optional.of(context), List.of(provider), (s, mfaProvider) -> false);
        assertNull(results);
    }

    private static void assertProviderResolutionFromManyProviders(final MultifactorAuthenticationTrigger trigger,
                                                                  final ConfigurableApplicationContext applicationContext,
                                                                  final boolean assertPresence) {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val provider1 = new TestMultifactorAuthenticationProvider();
        provider1.setOrder(10);
        registerProviderInApplicationContext(applicationContext, context, provider1);

        val provider2 = new TestMultifactorAuthenticationProvider("mfa-other");
        provider2.setOrder(1);
        registerProviderInApplicationContext(applicationContext, context, provider2);

        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            CollectionUtils.wrap("mfa-principal", List.of(provider2.getId())));
        val result = trigger.isActivated(CoreAuthenticationTestUtils.getAuthentication(principal,
            CollectionUtils.wrap("mfa-authn", List.of(provider2.getId()))),
            CoreAuthenticationTestUtils.getRegisteredService(), request, CoreAuthenticationTestUtils.getService());
        if (assertPresence) {
            assertTrue(result.isPresent());
            assertEquals(provider2.getId(), result.get().getId());
        } else {
            assertTrue(result.isEmpty());
        }
    }

    private static TestMultifactorAuthenticationProvider registerProviderInApplicationContext(final ConfigurableApplicationContext applicationContext,
                                                                                              final MockRequestContext context,
                                                                                              final TestMultifactorAuthenticationProvider candidateProvider) {
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext, candidateProvider);
        val targetResolver = new DefaultTargetStateResolver(provider.getId());
        val transition = new Transition(new DefaultTransitionCriteria(new LiteralExpression(provider.getId())), targetResolver);
        context.getRootFlow().getGlobalTransitionSet().add(transition);
        return (TestMultifactorAuthenticationProvider) provider;
    }
}
