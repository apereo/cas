package org.apereo.cas.web.flow;

import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.MockServletContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
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
import org.springframework.webflow.test.MockRequestContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientIdentityProviderConfigurationProducerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes =
    BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("Delegation")
class DelegatedClientIdentityProviderConfigurationProducerTests {
    @Autowired
    @Qualifier(DelegatedClientIdentityProviderConfigurationProducer.BEAN_NAME)
    private DelegatedClientIdentityProviderConfigurationProducer producer;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;
    
    @Test
    void verifyOperationAutoRedirect() throws Throwable {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
        val policy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        policy.setAllowedProviders(List.of("SAML2Client"));
        policy.setPermitUndefined(false);

        accessStrategy.setDelegatedAuthenticationPolicy(policy);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://delegated2.example.org");
        registeredService.setAccessStrategy(accessStrategy);
        servicesManager.save(registeredService);
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService("https://delegated2.example.org"));
        assertNotNull(producer.produce(context));
        assertNotNull(DelegationWebflowUtils.getDelegatedAuthenticationProviderConfigurations(context));
        assertNotNull(DelegationWebflowUtils.getDelegatedAuthenticationProviderPrimary(context));
    }

    @Test
    void verifyOperationPrimaryProvider() throws Throwable {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
        val policy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        policy.setAllowedProviders(List.of("CasClient"));
        policy.setPermitUndefined(false);
        accessStrategy.setDelegatedAuthenticationPolicy(policy);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://delegated.example.org");
        registeredService.setAccessStrategy(accessStrategy);
        servicesManager.save(registeredService);
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService("https://delegated.example.org"));
        assertNotNull(producer.produce(context));
        assertNotNull(DelegationWebflowUtils.getDelegatedAuthenticationProviderConfigurations(context));
        assertNotNull(DelegationWebflowUtils.getDelegatedAuthenticationProviderPrimary(context));
    }
}
