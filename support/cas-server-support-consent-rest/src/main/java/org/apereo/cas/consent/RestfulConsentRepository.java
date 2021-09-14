package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.consent.RestfulConsentProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.hjson.JsonValue;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * This is {@link RestfulConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class RestfulConsentRepository implements ConsentRepository {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true)
        .build()
        .toObjectMapper();

    private static final long serialVersionUID = 6583408864586270206L;

    private final RestfulConsentProperties properties;

    @Override
    @SneakyThrows
    public Collection<? extends ConsentDecision> findConsentDecisions(final String principal) {
        HttpResponse response = null;
        try {
            val headers = new HashMap<String, Object>();
            headers.put("Content-Type", MediaType.APPLICATION_JSON);
            headers.put("Accept", MediaType.APPLICATION_JSON);
            headers.put("principal", principal);
            headers.putAll(properties.getHeaders());

            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(properties.getUrl())
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
            if (HttpStatus.valueOf(response.getStatusLine().getStatusCode()).is2xxSuccessful()) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                return MAPPER.readValue(JsonValue.readHjson(result).toString(), List.class);
            }
        } finally {
            HttpUtils.close(response);
        }
        return new ArrayList<>(0);
    }

    @Override
    @SneakyThrows
    public Collection<? extends ConsentDecision> findConsentDecisions() {
        HttpResponse response = null;
        try {
            val headers = new HashMap<String, Object>();
            headers.put("Content-Type", MediaType.APPLICATION_JSON);
            headers.put("Accept", MediaType.APPLICATION_JSON);
            headers.putAll(properties.getHeaders());

            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(properties.getUrl())
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
            if (HttpStatus.valueOf(response.getStatusLine().getStatusCode()).is2xxSuccessful()) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                return MAPPER.readValue(result, List.class);
            }
        } finally {
            HttpUtils.close(response);
        }
        return new ArrayList<>(0);
    }

    @Override
    @SneakyThrows
    public ConsentDecision findConsentDecision(final Service service, final RegisteredService registeredService,
                                               final Authentication authentication) {
        HttpResponse response = null;
        try {
            val headers = new HashMap<String, Object>();
            headers.put("Content-Type", MediaType.APPLICATION_JSON);
            headers.put("Accept", MediaType.APPLICATION_JSON);
            headers.put("service", service.getId());
            headers.put("principal", authentication.getPrincipal().getId());
            headers.putAll(properties.getHeaders());

            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .method(HttpMethod.GET)
                .url(properties.getUrl())
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
            if (HttpStatus.valueOf(response.getStatusLine().getStatusCode()).is2xxSuccessful()) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                return MAPPER.readValue(result, ConsentDecision.class);
            }
        } finally {
            HttpUtils.close(response);
        }
        return null;
    }

    @Override
    @SneakyThrows
    public ConsentDecision storeConsentDecision(final ConsentDecision decision) {
        HttpResponse response = null;
        try {
            val headers = new HashMap<String, Object>();
            headers.put("Content-Type", MediaType.APPLICATION_JSON);
            headers.put("Accept", MediaType.APPLICATION_JSON);
            headers.putAll(properties.getHeaders());

            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .method(HttpMethod.POST)
                .url(properties.getUrl())
                .headers(headers)
                .entity(MAPPER.writeValueAsString(decision))
                .build();
            response = HttpUtils.execute(exec);
            if (HttpStatus.valueOf(response.getStatusLine().getStatusCode()).is2xxSuccessful()) {
                return decision;
            }
        } finally {
            HttpUtils.close(response);
        }
        return null;
    }

    @Override
    @SneakyThrows
    public boolean deleteConsentDecisions(final String principal) {
        HttpResponse response = null;
        try {
            val headers = new HashMap<String, Object>();
            headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            headers.put("Accept", MediaType.APPLICATION_JSON_VALUE);
            headers.put("principal", principal);
            headers.putAll(properties.getHeaders());
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .method(HttpMethod.DELETE)
                .url(properties.getUrl())
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
            return HttpStatus.valueOf(response.getStatusLine().getStatusCode()).is2xxSuccessful();
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    @SneakyThrows
    public boolean deleteConsentDecision(final long decisionId, final String principal) {
        HttpResponse response = null;
        try {
            val headers = new HashMap<String, Object>();
            headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            headers.put("Accept", MediaType.APPLICATION_JSON_VALUE);
            headers.put("principal", principal);
            headers.putAll(properties.getHeaders());
            val deleteEndpoint = properties.getUrl().concat('/' + Long.toString(decisionId));
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .method(HttpMethod.DELETE)
                .url(deleteEndpoint)
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
            return HttpStatus.valueOf(response.getStatusLine().getStatusCode()).is2xxSuccessful();
        } finally {
            HttpUtils.close(response);
        }
    }
}
