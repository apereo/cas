package org.apereo.cas.pac4j.discovery;

import lombok.val;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.ChainingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.EchoingPrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.pac4j.authentication.clients.RefreshableDelegatedIdentityProviders;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.core.io.ClassPathResource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultDelegatedAuthenticationDynamicDiscoveryProviderLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Delegation")
class DefaultDelegatedAuthenticationDynamicDiscoveryProviderLocatorTests {

    private CasConfigurationProperties properties;

    @BeforeEach
    public void setup() {
        this.properties = new CasConfigurationProperties();
    }

    private DelegatedAuthenticationDynamicDiscoveryProviderLocator getLocator(final Principal principal, final List<PrincipalResolver> principalResolvers)
        throws Throwable {
        val producer = mock(DelegatedClientIdentityProviderConfigurationProducer.class);

        val electionStrategy = mock(PrincipalElectionStrategy.class);
        final List<Principal> ts = anyList();
        when(electionStrategy.nominate(ts, any(Map.class))).thenReturn(principal);
        val resolver = new ChainingPrincipalResolver(electionStrategy, properties);
        resolver.setChain(principalResolvers);

        val client = new SAML2Client();
        val refreshableClients = new RefreshableDelegatedIdentityProviders("http://localhost:8080/cas",
            DelegatedIdentityProviderFactory.withClients(List.of(client)));
        val locator = new DefaultDelegatedAuthenticationDynamicDiscoveryProviderLocator(producer, refreshableClients, resolver, properties);
        assertNotNull(locator.getProviderProducer());
        assertNotNull(locator.getProperties());
        assertNotNull(locator.getIdentityProviders());
        assertNotNull(locator.getPrincipalResolver());
        return locator;
    }

    @Test
    void verifyResourceIsUnavailable() throws Throwable {
        val principal = RegisteredServiceTestUtils.getPrincipal("cas@example.org");
        val locator = getLocator(principal, List.of(new EchoingPrincipalResolver()));
        val request = DelegatedAuthenticationDynamicDiscoveryProviderLocator.DynamicDiscoveryProviderRequest
            .builder()
            .userId(principal.getId())
            .build();
        val result = locator.locate(request);
        assertFalse(result.isPresent());
    }

    @Test
    void verifyResourceFindUser() throws Throwable {
        val principal = RegisteredServiceTestUtils.getPrincipal("cas@example.org", Map.of("cn", List.of("cas", "casuser", "cas-user")));
        val locator = getLocator(principal, List.of(new EchoingPrincipalResolver()));

        val json = properties.getAuthn().getPac4j().getCore().getDiscoverySelection().getJson();
        json.setLocation(new ClassPathResource("delegated-discovery.json"));
        val request = DelegatedAuthenticationDynamicDiscoveryProviderLocator.DynamicDiscoveryProviderRequest
            .builder()
            .userId(principal.getId())
            .build();
        val result = locator.locate(request);
        assertTrue(result.isPresent());
    }

    @Test
    void verifyPrincipalAttribute() throws Throwable {
        final Map<String, List<Object>> principalAttributes = Map.of("email", List.of("cas@example.net", "casuser@example.org", "casuser@yahoo.com"));
        val principal = RegisteredServiceTestUtils.getPrincipal("cas@example.org", principalAttributes);
        val locator = getLocator(principal, List.of(new EchoingPrincipalResolver()));
        val json = properties.getAuthn().getPac4j().getCore().getDiscoverySelection().getJson();
        json.setLocation(new ClassPathResource("delegated-discovery.json"));
        json.setPrincipalAttribute("email");
        val request = DelegatedAuthenticationDynamicDiscoveryProviderLocator.DynamicDiscoveryProviderRequest
            .builder()
            .userId(principal.getId())
            .build();
        val result = locator.locate(request);
        assertTrue(result.isPresent());
    }
}
