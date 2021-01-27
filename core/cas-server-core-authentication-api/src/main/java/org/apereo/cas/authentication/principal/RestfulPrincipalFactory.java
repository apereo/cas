package org.apereo.cas.authentication.principal;

import org.apereo.cas.configuration.model.RestEndpointProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.hjson.JsonValue;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * This is {@link RestfulPrincipalFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class RestfulPrincipalFactory extends DefaultPrincipalFactory {
    private static final long serialVersionUID = -1344968589212057694L;

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final RestEndpointProperties properties;

    @Override
    public Principal createPrincipal(final String id, final Map<String, List<Object>> attributes) {
        HttpResponse response = null;
        try {
            val current = super.createPrincipal(id, attributes);
            val entity = MAPPER.writeValueAsString(current);

            val headers = CollectionUtils.<String, Object>wrap("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            headers.putAll(properties.getHeaders());

            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(properties.getBasicAuthPassword())
                .basicAuthUsername(properties.getBasicAuthUsername())
                .method(HttpMethod.POST)
                .url(properties.getUrl())
                .entity(entity)
                .headers(headers)
                .build();
            response = HttpUtils.execute(exec);
            if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.OK.value()) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                LOGGER.debug("Principal factory response received: [{}]", result);
                return MAPPER.readValue(JsonValue.readHjson(result).toString(), SimplePrincipal.class);
            }
        } catch (final Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
        LOGGER.error("Unable to create principal from REST endpoint [{}] for [{}]", properties.getUrl(), id);
        return null;
    }
}
