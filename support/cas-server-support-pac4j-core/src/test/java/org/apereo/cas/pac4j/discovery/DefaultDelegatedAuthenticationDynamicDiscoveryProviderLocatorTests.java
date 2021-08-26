package org.apereo.cas.pac4j.discovery;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationProducer;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.client.Clients;
import org.springframework.core.io.ClassPathResource;

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
    @Test
    public void verifyOperation() {
        val properties = new CasConfigurationProperties();
        val producer = mock(DelegatedClientIdentityProviderConfigurationProducer.class);
        val clients = mock(Clients.class);

        val client = new CasClient();
        when(clients.findClient(anyString())).thenReturn(Optional.of(client));
        val locator = new DefaultDelegatedAuthenticationDynamicDiscoveryProviderLocator(producer, clients, properties);
        assertNotNull(locator.getProviderProducer());
        assertNotNull(locator.getCasProperties());
        assertNotNull(locator.getClients());
        
        val request = DelegatedAuthenticationDynamicDiscoveryProviderLocator.DynamicDiscoveryProviderRequest
            .builder()
            .userId("cas@example.org")
            .build();
        var result = locator.locate(request);
        assertFalse(result.isPresent());

        properties.getAuthn().getPac4j().getCore()
            .getDiscoverySelection().getJson()
            .setLocation(new ClassPathResource("delegated-discovery.json"));
        result = locator.locate(request);
        assertTrue(result.isPresent());
    }
}
