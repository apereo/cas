package org.apereo.cas.web.flow.decorator;

import org.apereo.cas.configuration.model.core.web.flow.RestfulWebflowLoginDecoratorProperties;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.hjson.JsonValue;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.RequestContext;
import tools.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
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
    public void decorate(final RequestContext requestContext) {
        FunctionUtils.doUnchecked(_ -> {
            HttpResponse response = null;
            try {
                val exec = HttpExecutionRequest.builder()
                    .basicAuthPassword(restProperties.getBasicAuthPassword())
                    .basicAuthUsername(restProperties.getBasicAuthUsername())
                    .maximumRetryAttempts(restProperties.getMaximumRetryAttempts())
                    .method(HttpMethod.valueOf(restProperties.getMethod().toUpperCase(Locale.ENGLISH).trim()))
                    .url(restProperties.getUrl())
                    .headers(restProperties.getHeaders())
                    .build();
                response = HttpUtils.execute(exec);
                val statusCode = response.getCode();
                if (HttpStatus.valueOf(statusCode).is2xxSuccessful()) {
                    try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                        val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                        val jsonObject = MAPPER.readValue(JsonValue.readHjson(result).toString(), Map.class);
                        requestContext.getFlowScope().put("decoration", jsonObject);
                    }
                }
            } finally {
                HttpUtils.close(response);
            }
        });
    }
}
