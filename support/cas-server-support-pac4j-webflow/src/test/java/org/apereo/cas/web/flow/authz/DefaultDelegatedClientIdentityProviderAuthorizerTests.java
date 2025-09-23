package org.apereo.cas.web.flow.authz;

import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderAuthorizer;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.RequestContext;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultDelegatedClientIdentityProviderAuthorizerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Delegation")
class DefaultDelegatedClientIdentityProviderAuthorizerTests {
    @ExtendWith(CasTestExtension.class)
    @SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
    abstract static class BaseTests {
        @Autowired
        @Qualifier(DelegatedClientIdentityProviderAuthorizer.BEAN_NAME)
        protected DelegatedClientIdentityProviderAuthorizer delegatedClientIdentityProviderAuthorizer;

        @Autowired
        @Qualifier(DelegatedIdentityProviders.BEAN_NAME)
        protected DelegatedIdentityProviders identityProviders;

        @Autowired
        @Qualifier(ServicesManager.BEAN_NAME)
        protected ServicesManager servicesManager;

        @Autowired
        protected ConfigurableApplicationContext applicationContext;

        @BeforeEach
        void setup() {
            servicesManager.deleteAll();
        }

        protected void verifyAuthorizationForService(final RequestContext requestContext) throws Throwable {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            val webContext = new JEEContext(request, response);
            
            val client = identityProviders.findClient("CasClient", webContext).orElseThrow();
            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            assertTrue(delegatedClientIdentityProviderAuthorizer.isDelegatedClientAuthorizedForService(client, null, request));
            assertFalse(delegatedClientIdentityProviderAuthorizer.isDelegatedClientAuthorizedForService(client, service, request));
            assertFalse(delegatedClientIdentityProviderAuthorizer.isDelegatedClientAuthorizedForService(client, service, requestContext));

            val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of());
            registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy().setEnabled(false));
            servicesManager.save(registeredService);
            assertFalse(delegatedClientIdentityProviderAuthorizer.isDelegatedClientAuthorizedForService(client, service, request));
            assertFalse(delegatedClientIdentityProviderAuthorizer.isDelegatedClientAuthorizedForService(client, service, requestContext));

            val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
            registeredService.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy().setEnabled(true));
            val delegationStrategy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy()
                .setAllowedProviders(List.of("AnotherClient"));
            accessStrategy.setDelegatedAuthenticationPolicy(delegationStrategy);
            registeredService.setAccessStrategy(accessStrategy);
            servicesManager.save(registeredService);
            assertFalse(delegatedClientIdentityProviderAuthorizer.isDelegatedClientAuthorizedForService(client, service, request));
            assertFalse(delegatedClientIdentityProviderAuthorizer.isDelegatedClientAuthorizedForService(client, service, requestContext));

            delegationStrategy.setAllowedProviders(List.of(client.getName()));
            servicesManager.save(registeredService);
            assertTrue(delegatedClientIdentityProviderAuthorizer.isDelegatedClientAuthorizedForService(client, service, request));
            assertTrue(delegatedClientIdentityProviderAuthorizer.isDelegatedClientAuthorizedForService(client, service, requestContext));
        }
    }

    @Nested
    class DefaultTests extends BaseTests {
        @Test
        void verifyClientNameFromAuth() throws Throwable {
            val requestContext = MockRequestContext.create(applicationContext);
            val webContext = new JEEContext(requestContext.getHttpServletRequest(), requestContext.getHttpServletResponse());

            val client = identityProviders.findClient("CasClient", webContext).orElseThrow();
            val authn = RegisteredServiceTestUtils.getAuthentication("casuser",
                Map.of(ClientCredential.AUTHENTICATION_ATTRIBUTE_CLIENT_NAME, List.of(client.getName())));

            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            val accessStrategy = new DefaultRegisteredServiceAccessStrategy();
            val delegationStrategy = new DefaultRegisteredServiceDelegatedAuthenticationPolicy()
                .setAllowedProviders(List.of(client.getName()));
            accessStrategy.setDelegatedAuthenticationPolicy(delegationStrategy);
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of());
            registeredService.setAccessStrategy(accessStrategy);
            servicesManager.save(registeredService);
            assertTrue(delegatedClientIdentityProviderAuthorizer.isDelegatedClientAuthorizedForAuthentication(authn, service, requestContext));
        }

        @Test
        void verifyAuthorizationByService() throws Throwable {
            val requestContext = MockRequestContext.create(applicationContext);
            verifyAuthorizationForService(requestContext);
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.multitenancy.core.enabled=true",
        "cas.multitenancy.json.location=classpath:/tenants.json"
    })
    class MultitenancyTests extends BaseTests {
        @Test
        void verifyAuthorizationByTenant() throws Throwable {
            val requestContext = MockRequestContext.create(applicationContext);
            requestContext.setContextPath("/tenants/shire/login");
            val request = requestContext.getHttpServletRequest();

            val webContext = new JEEContext(requestContext.getHttpServletRequest(), requestContext.getHttpServletResponse());

            var client = identityProviders.findClient("CasClient", webContext).orElseThrow();
            val service = RegisteredServiceTestUtils.getService(UUID.randomUUID().toString());
            val registeredService = RegisteredServiceTestUtils.getRegisteredService(service.getId(), Map.of());
            servicesManager.save(registeredService);
            assertTrue(delegatedClientIdentityProviderAuthorizer.isDelegatedClientAuthorizedForService(client, service, request));

            client = identityProviders.findClient("LogoutClient", webContext).orElseThrow();
            assertFalse(delegatedClientIdentityProviderAuthorizer.isDelegatedClientAuthorizedForService(client, service, request));
        }
    }

}
