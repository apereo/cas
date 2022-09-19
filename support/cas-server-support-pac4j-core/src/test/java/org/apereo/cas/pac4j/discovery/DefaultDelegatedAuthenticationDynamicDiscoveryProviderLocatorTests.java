package org.apereo.cas.pac4j.discovery;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationProducer;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.client.Clients;
import org.springframework.core.io.ClassPathResource;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultDelegatedAuthenticationDynamicDiscoveryProviderLocatorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Delegation")
public class DefaultDelegatedAuthenticationDynamicDiscoveryProviderLocatorTests {
    private CasConfigurationProperties properties;

    @BeforeEach
    public void setup() {
        this.properties = new CasConfigurationProperties();
    }

    private DelegatedAuthenticationDynamicDiscoveryProviderLocator getLocator(final Principal principal) {
        val producer = mock(DelegatedClientIdentityProviderConfigurationProducer.class);
        val clients = mock(Clients.class);

        val resolver = mock(PrincipalResolver.class);
        when(resolver.resolve(any(Credential.class))).thenReturn(principal);

        val client = new CasClient();
        when(clients.findClient(anyString())).thenReturn(Optional.of(client));
        val locator = new DefaultDelegatedAuthenticationDynamicDiscoveryProviderLocator(producer, clients, resolver, properties);
        assertNotNull(locator.getProviderProducer());
        assertNotNull(locator.getProperties());
        assertNotNull(locator.getClients());
        assertNotNull(locator.getPrincipalResolver());
        return locator;
    }

    @Test
    public void verifyResourceIsUnavailable() {
        val principal = RegisteredServiceTestUtils.getPrincipal("cas@example.org");
        val locator = getLocator(principal);
        val request = DelegatedAuthenticationDynamicDiscoveryProviderLocator.DynamicDiscoveryProviderRequest
            .builder()
            .userId(principal.getId())
            .build();
        var result = locator.locate(request);
        assertFalse(result.isPresent());
    }

    @Test
    public void verifyResourceFindUser() {
        val principal = RegisteredServiceTestUtils.getPrincipal("cas@example.org", Map.of("cn", List.of("cas", "casuser", "cas-user")));
        val locator = getLocator(principal);
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
    public void verifyPrincipalAttribute() {
        val principal = RegisteredServiceTestUtils.getPrincipal("cas@example.org",
            Map.of("email", List.of("cas@example.net", "casuser@example.org", "casuser@yahoo.com")));
        val locator = getLocator(principal);
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
