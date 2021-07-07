package org.apereo.cas.support.pac4j.authentication;

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
import org.pac4j.core.client.Client;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link RestfulDelegatedClientFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
public class RestfulDelegatedClientFactory implements DelegatedClientFactory<Client> {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final CasConfigurationProperties casProperties;

    private final Cache<String, Collection<Client>> clientsCache;

    public RestfulDelegatedClientFactory(final CasConfigurationProperties casProperties) {
        this.casProperties = casProperties;

        val restProperties = casProperties.getAuthn().getPac4j().getRest();
        this.clientsCache = Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterAccess(Beans.newDuration(restProperties.getCacheDuration()))
            .build();
    }

    @Override
    public Collection<Client> build() {
        val cachedClients = clientsCache.getIfPresent(casProperties.getServer().getName());

        if (cachedClients == null) {
            val restProperties = casProperties.getAuthn().getPac4j().getRest();
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .method(HttpMethod.valueOf(restProperties.getMethod().toUpperCase().trim()))
                .url(restProperties.getUrl())
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
                        val clients = MAPPER.readValue(JsonValue.readHjson(result).toString(), Map.class);
                        LOGGER.trace("Delegated clients received from [{}] are [{}]", restProperties.getUrl(), clients);
                        val callbackUrl = (String) clients.getOrDefault("callbackUrl", null);
                        val properties = (Map<String, String>)
                            clients.getOrDefault("properties", new HashMap<String, String>(0));
                        val factory = new PropertiesConfigFactory(callbackUrl, properties);
                        val builtClients = factory.build().getClients().findAllClients();
                        clientsCache.put(casProperties.getServer().getName(), builtClients);
                        return builtClients;
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
}
