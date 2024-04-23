package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.consent.RestfulConsentProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.hjson.JsonValue;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import java.io.Serial;
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

    @Serial
    private static final long serialVersionUID = 6583408864586270206L;

    private final RestfulConsentProperties properties;

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions(final String principal) {
        return FunctionUtils.doUnchecked(() -> {
            HttpResponse response = null;
            try {
                val headers = new HashMap<String, String>();
                headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                headers.put("Accept", MediaType.APPLICATION_JSON_VALUE);
                headers.put("principal", principal);
                headers.putAll(properties.getHeaders());

                val exec = HttpExecutionRequest.builder()
                    .basicAuthPassword(properties.getBasicAuthPassword())
                    .basicAuthUsername(properties.getBasicAuthUsername())
                    .method(HttpMethod.GET)
                    .url(properties.getUrl())
                    .headers(headers)
                    .build();
                response = HttpUtils.execute(exec);
                if (response != null && HttpStatus.valueOf(response.getCode()).is2xxSuccessful()) {
                    try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                        val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                        val expectedType = MAPPER.getTypeFactory().constructParametricType(List.class, ConsentDecision.class);
                        return MAPPER.readValue(JsonValue.readHjson(result).toString(), expectedType);
                    }
                }
            } finally {
                HttpUtils.close(response);
            }
            return new ArrayList<>(0);
        });
    }

    @Override
    public Collection<? extends ConsentDecision> findConsentDecisions() {
        return FunctionUtils.doUnchecked(() -> {
            HttpResponse response = null;
            try {
                val headers = new HashMap<String, String>();
                headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                headers.put("Accept", MediaType.APPLICATION_JSON_VALUE);
                headers.putAll(properties.getHeaders());

                val exec = HttpExecutionRequest.builder()
                    .basicAuthPassword(properties.getBasicAuthPassword())
                    .basicAuthUsername(properties.getBasicAuthUsername())
                    .method(HttpMethod.GET)
                    .url(properties.getUrl())
                    .headers(headers)
                    .build();
                response = HttpUtils.execute(exec);
                if (response != null && HttpStatus.valueOf(response.getCode()).is2xxSuccessful()) {
                    try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                        val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                        val expectedType = MAPPER.getTypeFactory().constructParametricType(List.class, ConsentDecision.class);
                        return MAPPER.readValue(result, expectedType);
                    }
                }
            } finally {
                HttpUtils.close(response);
            }
            return new ArrayList<>(0);
        });
    }

    @Override
    public ConsentDecision findConsentDecision(final Service service, final RegisteredService registeredService,
                                               final Authentication authentication) {
        return FunctionUtils.doUnchecked(() -> {
            HttpResponse response = null;
            try {
                val headers = new HashMap<String, String>();
                headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                headers.put("Accept", MediaType.APPLICATION_JSON_VALUE);
                headers.put("service", service.getId());
                headers.put("principal", authentication.getPrincipal().getId());
                headers.putAll(properties.getHeaders());

                val exec = HttpExecutionRequest.builder()
                    .basicAuthPassword(properties.getBasicAuthPassword())
                    .basicAuthUsername(properties.getBasicAuthUsername())
                    .method(HttpMethod.GET)
                    .url(properties.getUrl())
                    .headers(headers)
                    .build();
                response = HttpUtils.execute(exec);
                if (response != null && HttpStatus.valueOf(response.getCode()).is2xxSuccessful()) {
                    try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                        val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                        return MAPPER.readValue(result, ConsentDecision.class);
                    }
                }
            } finally {
                HttpUtils.close(response);
            }
            return null;
        });
    }

    @Override
    public ConsentDecision storeConsentDecision(final ConsentDecision decision) {
        return FunctionUtils.doUnchecked(() -> {
            HttpResponse response = null;
            try {
                val headers = new HashMap<String, String>();
                headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                headers.put("Accept", MediaType.APPLICATION_JSON_VALUE);
                headers.putAll(properties.getHeaders());

                val exec = HttpExecutionRequest.builder()
                    .basicAuthPassword(properties.getBasicAuthPassword())
                    .basicAuthUsername(properties.getBasicAuthUsername())
                    .method(HttpMethod.POST)
                    .url(properties.getUrl())
                    .headers(headers)
                    .entity(MAPPER.writeValueAsString(decision))
                    .build();
                response = HttpUtils.execute(exec);
                if (HttpStatus.valueOf(response.getCode()).is2xxSuccessful()) {
                    return decision;
                }
            } finally {
                HttpUtils.close(response);
            }
            return null;
        });
    }

    @Override
    public boolean deleteConsentDecisions(final String principal) {
        return FunctionUtils.doUnchecked(() -> {
            HttpResponse response = null;
            try {
                val headers = new HashMap<String, String>();
                headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                headers.put("Accept", MediaType.APPLICATION_JSON_VALUE);
                headers.put("principal", principal);
                headers.putAll(properties.getHeaders());
                val exec = HttpExecutionRequest.builder()
                    .basicAuthPassword(properties.getBasicAuthPassword())
                    .basicAuthUsername(properties.getBasicAuthUsername())
                    .method(HttpMethod.DELETE)
                    .url(properties.getUrl())
                    .headers(headers)
                    .build();
                response = HttpUtils.execute(exec);
                return response != null && HttpStatus.valueOf(response.getCode()).is2xxSuccessful();
            } finally {
                HttpUtils.close(response);
            }
        });
    }

    @Override
    public void deleteAll() {
        HttpResponse response = null;
        try {
            val headers = new HashMap<String, String>();
            headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            headers.put("Accept", MediaType.APPLICATION_JSON_VALUE);
            headers.putAll(properties.getHeaders());
            val exec = HttpExecutionRequest.builder()
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .method(HttpMethod.DELETE)
                .url(properties.getUrl())
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
        } finally {
            HttpUtils.close(response);
        }
    }

    @Override
    public boolean deleteConsentDecision(final long decisionId, final String principal) {
        return FunctionUtils.doUnchecked(() -> {
            HttpResponse response = null;
            try {
                val headers = new HashMap<String, String>();
                headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                headers.put("Accept", MediaType.APPLICATION_JSON_VALUE);
                headers.put("principal", principal);
                headers.putAll(properties.getHeaders());
                val deleteEndpoint = properties.getUrl().concat('/' + Long.toString(decisionId));
                val exec = HttpExecutionRequest.builder()
                    .basicAuthPassword(properties.getBasicAuthPassword())
                    .basicAuthUsername(properties.getBasicAuthUsername())
                    .method(HttpMethod.DELETE)
                    .url(deleteEndpoint)
                    .headers(headers)
                    .build();
                response = HttpUtils.execute(exec);
                return response != null && HttpStatus.valueOf(response.getCode()).is2xxSuccessful();
            } finally {
                HttpUtils.close(response);
            }
        });
    }
}
