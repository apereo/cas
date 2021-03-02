package org.apereo.cas.web.flow.decorator;

import org.apereo.cas.configuration.model.core.web.flow.RestfulWebflowLoginDecoratorProperties;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.hjson.JsonValue;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.RequestContext;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * This is {@link RestfulLoginWebflowDecorator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
public class RestfulLoginWebflowDecorator implements WebflowDecorator {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final RestfulWebflowLoginDecoratorProperties restProperties;

    @Override
    @SneakyThrows
    public void decorate(final RequestContext requestContext, final ApplicationContext applicationContext) {
        HttpResponse response = null;
        try {
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .method(HttpMethod.valueOf(restProperties.getMethod().toUpperCase().trim()))
                .url(restProperties.getUrl())
                .build();
            response = HttpUtils.execute(exec);
            val statusCode = response.getStatusLine().getStatusCode();
            if (HttpStatus.valueOf(statusCode).is2xxSuccessful()) {
                val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                val jsonObject = MAPPER.readValue(JsonValue.readHjson(result).toString(), Map.class);
                requestContext.getFlowScope().put("decoration", jsonObject);
            }
        } finally {
            HttpUtils.close(response);
        }
    }
}
