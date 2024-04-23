package org.apereo.cas.web.flow.authz;

import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderAuthorizer;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.execution.RequestContext;
import jakarta.servlet.http.HttpServletRequest;
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
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@Tag("Delegation")
class DefaultDelegatedClientIdentityProviderAuthorizerTests {
    @Autowired
    @Qualifier("delegatedClientIdentityProviderAuthorizer")
    private DelegatedClientIdentityProviderAuthorizer delegatedClientIdentityProviderAuthorizer;

    @Autowired
    @Qualifier(DelegatedIdentityProviders.BEAN_NAME)
    private DelegatedIdentityProviders identityProviders;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    private ServicesManager servicesManager;
    
    @BeforeEach
    public void setup() {
        servicesManager.deleteAll();
    }

    @Test
    void verifyClientNameFromAuth() throws Throwable {
        val client = identityProviders.findClient("FacebookClient").get();
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
        assertTrue(delegatedClientIdentityProviderAuthorizer.isDelegatedClientAuthorizedForAuthentication(authn, service, new MockRequestContext()));
    }

    @Test
    void verifyAuthzByService() throws Throwable {
        verifyAuthzForService(new MockHttpServletRequest(), new MockRequestContext());
    }

    private void verifyAuthzForService(final HttpServletRequest request,
                                       final RequestContext requestContext) throws Throwable {
        val client = identityProviders.findClient("FacebookClient").get();
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
