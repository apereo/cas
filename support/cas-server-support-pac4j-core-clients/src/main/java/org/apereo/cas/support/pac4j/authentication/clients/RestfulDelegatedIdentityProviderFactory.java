package org.apereo.cas.support.pac4j.authentication.clients;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.hjson.JsonValue;
import org.pac4j.config.client.PropertiesConfigFactory;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.IndirectClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link RestfulDelegatedIdentityProviderFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
public class RestfulDelegatedIdentityProviderFactory extends BaseDelegatedIdentityProviderFactory {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    public RestfulDelegatedIdentityProviderFactory(final Collection<DelegatedClientFactoryCustomizer> customizers,
                                                   final CasSSLContext casSSLContext,
                                                   final CasConfigurationProperties casProperties,
                                                   final Cache<String, List<BaseClient>> clientsCache,
                                                   final ConfigurableApplicationContext applicationContext) {
        super(casProperties, customizers, casSSLContext, clientsCache, applicationContext);
    }

    @Override
    protected List<BaseClient> loadIdentityProviders() throws Exception {
        val restProperties = casProperties.getAuthn().getPac4j().getRest();
        val exec = HttpExecutionRequest
            .builder()
            .basicAuthPassword(restProperties.getBasicAuthPassword())
            .basicAuthUsername(restProperties.getBasicAuthUsername())
            .method(HttpMethod.valueOf(restProperties.getMethod().toUpperCase(Locale.ENGLISH).trim()))
            .url(restProperties.getUrl())
            .parameters(Map.of("type", restProperties.getType()))
            .headers(restProperties.getHeaders())
            .build();

        val response = HttpUtils.execute(exec);
        if (response != null && HttpStatus.valueOf(response.getCode()).is2xxSuccessful()) {
            try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                if ("cas".equalsIgnoreCase(restProperties.getType())) {
                    return buildClientsBasedCasProperties(result);
                }
                return buildClientsBasedPac4jProperties(result);
            } finally {
                HttpUtils.close(response);
            }
        }
        return new ArrayList<>();
    }

    protected List<BaseClient> buildClientsBasedCasProperties(final String result) throws Exception {
        val payload = MAPPER.readValue(JsonValue.readHjson(result).toString(), Map.class);
        LOGGER.trace("CAS properties received as [{}]", payload);
        val properties = CasConfigurationProperties.bindFrom(payload);
        if (properties.isPresent()) {
            val props = (CasConfigurationProperties) properties.get();
            return buildFrom(props);
        }
        return List.of();
    }

    protected List<BaseClient> buildClientsBasedPac4jProperties(final String result) throws Exception {
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
