package org.apereo.cas.web.flow;

import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientIdentityProviderConfigurationFunctionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes =
    BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("Webflow")
public class DelegatedClientIdentityProviderConfigurationFunctionTests {
    @Autowired
    @Qualifier("delegatedClientIdentityProviderConfigurationFunction")
    private Function<RequestContext, Set<DelegatedClientIdentityProviderConfiguration>> function;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;
    
    @Test
    public void verifyOperationAutoRedirect() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
        accessStrategy.setDelegatedAuthenticationPolicy(
            new DefaultRegisteredServiceDelegatedAuthenticationPolicy(List.of("SAML2Client"), false, false));
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://delegated2.example.org");
        registeredService.setAccessStrategy(accessStrategy);
        servicesManager.save(registeredService);
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService("https://delegated2.example.org"));
        assertNotNull(function.apply(context));
        assertNotNull(WebUtils.getDelegatedAuthenticationProviderConfigurations(context));
        assertNotNull(WebUtils.getDelegatedAuthenticationProviderPrimary(context));
    }

    @Test
    public void verifyOperationPrimaryProvider() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
        accessStrategy.setDelegatedAuthenticationPolicy(
            new DefaultRegisteredServiceDelegatedAuthenticationPolicy(List.of("CasClient"), false, true));
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://delegated.example.org");
        registeredService.setAccessStrategy(accessStrategy);
        servicesManager.save(registeredService);
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService("https://delegated.example.org"));
        assertNotNull(function.apply(context));
        assertNotNull(WebUtils.getDelegatedAuthenticationProviderConfigurations(context));
        assertNotNull(WebUtils.getDelegatedAuthenticationProviderPrimary(context));
    }
}
