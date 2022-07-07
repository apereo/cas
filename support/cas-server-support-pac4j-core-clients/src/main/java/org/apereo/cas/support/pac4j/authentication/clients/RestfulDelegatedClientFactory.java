package org.apereo.cas.support.pac4j.authentication.clients;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.hjson.JsonValue;
import org.pac4j.config.client.PropertiesConfigFactory;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.saml.store.SAMLMessageStoreFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link RestfulDelegatedClientFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
public class RestfulDelegatedClientFactory extends BaseDelegatedClientFactory {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    public RestfulDelegatedClientFactory(final Collection<DelegatedClientFactoryCustomizer> customizers,
                                         final CasSSLContext casSSLContext,
                                         final CasConfigurationProperties casProperties,
                                         final ObjectProvider<SAMLMessageStoreFactory> samlMessageStoreFactory,
                                         final Cache<String, Collection<IndirectClient>> clientsCache) {
        super(casProperties, customizers, casSSLContext, samlMessageStoreFactory, clientsCache);
    }

    @Override
    protected Collection<IndirectClient> loadClients() {
        val restProperties = casProperties.getAuthn().getPac4j().getRest();
        val exec = HttpUtils.HttpExecutionRequest.builder()
            .basicAuthPassword(restProperties.getBasicAuthPassword())
            .basicAuthUsername(restProperties.getBasicAuthUsername())
            .method(HttpMethod.valueOf(restProperties.getMethod().toUpperCase().trim()))
            .url(restProperties.getUrl())
            .parameters(Map.of("type", restProperties.getType()))
            .headers(restProperties.getHeaders())
            .build();

        return FunctionUtils.doAndRetry(callback -> {
            val response = HttpUtils.execute(exec);
            try {
                if (response != null && HttpStatus.valueOf(response.getStatusLine().getStatusCode()).is2xxSuccessful()) {
                    val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                    switch (restProperties.getType().toLowerCase()) {
                        case "cas":
                            return buildClientsBasedCasProperties(result);
                        case "pac4j":
                        default:
                            return buildClientsBasedPac4jProperties(result);
                    }
                }
                return new ArrayList<>();
            } finally {
                HttpUtils.close(response);
            }
        });
    }

    protected Collection<IndirectClient> buildClientsBasedCasProperties(final String result) throws Exception {
        val payload = MAPPER.readValue(JsonValue.readHjson(result).toString(), Map.class);
        LOGGER.trace("CAS properties received as [{}]", payload);
        val binder = new Binder(ConfigurationPropertySources.from(new MapPropertySource(getClass().getSimpleName(), payload)));
        val bound = binder.bind(CasConfigurationProperties.PREFIX, Bindable.of(CasConfigurationProperties.class));
        if (bound.isBound()) {
            val properties = bound.get();
            return buildAllIdentityProviders(properties);
        }
        return List.of();
    }

    protected List<IndirectClient> buildClientsBasedPac4jProperties(final String result) throws Exception {
        val clients = MAPPER.readValue(JsonValue.readHjson(result).toString(), Map.class);
        LOGGER.trace("Delegated clients received are [{}]", clients);
        val callbackUrl = (String) clients.getOrDefault("callbackUrl", null);
        val properties = (Map<String, String>)
            clients.getOrDefault("properties", new HashMap<String, String>(0));
        val factory = new PropertiesConfigFactory(callbackUrl, properties);
        return factory.build()
            .getClients()
            .findAllClients()
            .stream()
            .map(IndirectClient.class::cast)
            .collect(Collectors.toList());
    }
}
