package org.apereo.cas.authentication;

import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAuditConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

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
    MailSenderAutoConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    CasCoreMultifactorAuthenticationAuditConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCookieConfiguration.class,
    CasPersonDirectoryConfiguration.class
})
@DirtiesContext
@Tag("MFA")
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
        var results = resolver.resolveEventViaPrincipalAttribute(principal,
            List.of("authlevel"), CoreAuthenticationTestUtils.getRegisteredService(),
            Optional.of(context), List.of(provider), input -> input.equalsIgnoreCase(provider.getId()));
        assertNotNull(results);
        assertNotNull(resolver.getMultifactorAuthenticationProviderSelector());
        assertEquals(provider.getId(), results.iterator().next().getId());

        results = resolver.resolveEventViaPrincipalAttribute(principal,
            List.of("authlevel"), CoreAuthenticationTestUtils.getRegisteredService(),
            Optional.of(context), List.of(), input -> input.equalsIgnoreCase(provider.getId()));
        assertNull(results);

        results = resolver.resolveEventViaPrincipalAttribute(principal,
            List.of(), CoreAuthenticationTestUtils.getRegisteredService(),
            Optional.of(context), List.of(), input -> input.equalsIgnoreCase(provider.getId()));
        assertNull(results);
    }

    @Test
    public void verifyNoProvider() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        val selector = mock(MultifactorAuthenticationProviderSelector.class);
        val resolver = new DefaultMultifactorAuthenticationProviderResolver(selector);
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val results = resolver.resolveEventViaAttribute(principal,
            Map.of("authlevel", List.of("strong")),
            List.of(), CoreAuthenticationTestUtils.getRegisteredService(),
            Optional.of(context), List.of(), Predicate.isEqual(this));
        assertNull(results);
    }

    @Test
    public void verifyNoMatch() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val selector = mock(MultifactorAuthenticationProviderSelector.class);
        when(selector.resolve(any(), any(), any())).thenReturn(provider);
        val resolver = new DefaultMultifactorAuthenticationProviderResolver(selector);
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        val results = resolver.resolveEventViaAttribute(principal,
            Map.of("authlevel", List.of("strong")),
            List.of(), CoreAuthenticationTestUtils.getRegisteredService(),
            Optional.of(context), List.of(provider), Predicate.isEqual(this));
        assertNull(results);
    }
}
