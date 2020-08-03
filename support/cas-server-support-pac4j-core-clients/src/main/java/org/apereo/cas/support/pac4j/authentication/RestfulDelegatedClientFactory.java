package org.apereo.cas.support.pac4j.authentication;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.HttpUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.hjson.JsonValue;
import org.pac4j.config.client.PropertiesConfigFactory;
import org.pac4j.core.client.Client;
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
@RequiredArgsConstructor
public class RestfulDelegatedClientFactory implements DelegatedClientFactory<Client> {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final CasConfigurationProperties casProperties;

    @SneakyThrows
    @Override
    public Collection<Client> build() {
        HttpResponse response = null;
        try {
            val restProperties = casProperties.getAuthn().getPac4j().getRest();
            response = HttpUtils.execute(restProperties.getUrl(), restProperties.getMethod(),
                restProperties.getBasicAuthUsername(), restProperties.getBasicAuthPassword());
            val statusCode = response.getStatusLine().getStatusCode();
            if (HttpStatus.valueOf(statusCode).is2xxSuccessful()) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                val clients = MAPPER.readValue(JsonValue.readHjson(result).toString(), Map.class);
                LOGGER.trace("Delegated clients received from [{}] are [{}]", restProperties.getUrl(), clients);
                val callbackUrl = (String) clients.getOrDefault("callbackUrl", null);
                val properties = (Map<String, String>)
                    clients.getOrDefault("properties", new HashMap<String, String>(0));
                val factory = new PropertiesConfigFactory(callbackUrl, properties);
                return factory.build().getClients().findAllClients();
            }
        } finally {
            HttpUtils.close(response);
        }
        return new ArrayList<>(0);
    }
}
