package org.apereo.cas.authentication;

import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAuditConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultMultifactorAuthenticationProviderResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasCoreMultifactorAuthenticationAuditConfiguration.class
})
@DirtiesContext
public class DefaultMultifactorAuthenticationProviderResolverTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyResolutionByAuthenticationAttribute() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val targetResolver = new DefaultTargetStateResolver(TestMultifactorAuthenticationProvider.ID);
        val transition = new Transition(new DefaultTransitionCriteria(new LiteralExpression(TestMultifactorAuthenticationProvider.ID)), targetResolver);
        context.getRootFlow().getGlobalTransitionSet().add(transition);

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

        val selector = mock(MultifactorAuthenticationProviderSelector.class);
        when(selector.resolve(any(), any(), any())).thenReturn(provider);

        val resolver = new DefaultMultifactorAuthenticationProviderResolver(selector);
        
        val authentication = CoreAuthenticationTestUtils.getAuthentication("casuser", CollectionUtils.wrap("authlevel", List.of(provider.getId())));
        val results = resolver.resolveEventViaAuthenticationAttribute(authentication,
            List.of("authlevel"), CoreAuthenticationTestUtils.getRegisteredService(),
            Optional.of(context), List.of(provider), input -> input.equalsIgnoreCase(provider.getId()));
        assertNotNull(results);
        assertEquals(provider.getId(), results.iterator().next().getId());
    }

    @Test
    public void verifyResolutionByPrincipalAttribute() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val targetResolver = new DefaultTargetStateResolver(TestMultifactorAuthenticationProvider.ID);
        val transition = new Transition(new DefaultTransitionCriteria(new LiteralExpression(TestMultifactorAuthenticationProvider.ID)), targetResolver);
        context.getRootFlow().getGlobalTransitionSet().add(transition);

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

        val selector = mock(MultifactorAuthenticationProviderSelector.class);
        when(selector.resolve(any(), any(), any())).thenReturn(provider);

        val resolver = new DefaultMultifactorAuthenticationProviderResolver(selector);

        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("authlevel", List.of(provider.getId())));
        val results = resolver.resolveEventViaPrincipalAttribute(principal,
            List.of("authlevel"), CoreAuthenticationTestUtils.getRegisteredService(),
            Optional.of(context), List.of(provider), input -> input.equalsIgnoreCase(provider.getId()));
        assertNotNull(results);
        assertEquals(provider.getId(), results.iterator().next().getId());
    }
}
