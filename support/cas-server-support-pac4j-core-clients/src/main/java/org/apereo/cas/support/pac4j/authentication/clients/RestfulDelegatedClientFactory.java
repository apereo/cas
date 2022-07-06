package org.apereo.cas.support.pac4j.authentication.clients;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
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

    private final CasConfigurationProperties casProperties;

    private final Cache<String, Collection<IndirectClient>> clientsCache;

    public RestfulDelegatedClientFactory(final Collection<DelegatedClientFactoryCustomizer> customizers,
                                         final CasSSLContext casSSLContext,
                                         final CasConfigurationProperties casProperties,
                                         final ObjectProvider<SAMLMessageStoreFactory> samlMessageStoreFactory) {
        super(customizers, casSSLContext, samlMessageStoreFactory);
        this.casProperties = casProperties;

        val rest = casProperties.getAuthn().getPac4j().getRest();
        this.clientsCache = Caffeine.newBuilder()
            .maximumSize(rest.getCacheSize())
            .expireAfterAccess(Beans.newDuration(rest.getCacheDuration()))
            .build();
    }

    @Override
    public Collection<IndirectClient> build() {
        val cachedClients = clientsCache.getIfPresent(casProperties.getServer().getName());

        if (cachedClients == null) {
            val restProperties = casProperties.getAuthn().getPac4j().getRest();
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .method(HttpMethod.valueOf(restProperties.getMethod().toUpperCase().trim()))
                .url(restProperties.getUrl())
                .parameters(Map.of("type", restProperties.getType()))
                .headers(restProperties.getHeaders())
                .build();

            try {
                return FunctionUtils.doAndRetry(callback -> {
                    val response = HttpUtils.execute(exec);
                    try {
                        val statusCode = response.getStatusLine().getStatusCode();
                        if (!HttpStatus.valueOf(statusCode).is2xxSuccessful()) {
                            throw new RuntimeException("Unable to retrieve delegated clients with status code " + statusCode);
                        }
                        val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                        switch (restProperties.getType().toLowerCase()) {
                            case "cas":
                                return buildClientsBasedCasProperties(result);
                            case "pac4j":
                            default:
                                return buildClientsBasedPac4jProperties(result);
                        }
                    } finally {
                        HttpUtils.close(response);
                    }
                });
            } catch (final Exception e) {
                LoggingUtils.warn(LOGGER, e);
            }
        }

        return ObjectUtils.defaultIfNull(cachedClients, new ArrayList<>());
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
        val builtClients = factory.build()
            .getClients()
            .findAllClients()
            .stream()
            .map(IndirectClient.class::cast)
            .collect(Collectors.toList());
        clientsCache.put(casProperties.getServer().getName(), builtClients);
        return builtClients;
    }
}
