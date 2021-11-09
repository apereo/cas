package org.apereo.cas.pac4j.discovery;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationProducer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link DefaultDelegatedAuthenticationDynamicDiscoveryProviderLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public class DefaultDelegatedAuthenticationDynamicDiscoveryProviderLocator implements DelegatedAuthenticationDynamicDiscoveryProviderLocator {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private final DelegatedClientIdentityProviderConfigurationProducer providerProducer;

    private final Clients clients;

    private final CasConfigurationProperties casProperties;

    @Override
    public Optional<IndirectClient> locate(final DynamicDiscoveryProviderRequest request) {
        try {
            val resource = casProperties.getAuthn().getPac4j().getCore().getDiscoverySelection().getJson().getLocation();
            val mappings = MAPPER.readValue(resource.getInputStream(),
                new TypeReference<Map<String, DelegatedAuthenticationDynamicDiscoveryProvider>>() {
                });

            return mappings
                .entrySet()
                .stream()
                .sorted(Comparator.comparingInt(o -> o.getValue().getOrder()))
                .filter(entry -> RegexUtils.find(entry.getKey(), request.getUserId()))
                .map(Map.Entry::getValue)
                .map(provider -> clients.findClient(provider.getClientName()))
                .flatMap(Optional::stream)
                .map(IndirectClient.class::cast)
                .findFirst();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return Optional.empty();
    }
}
