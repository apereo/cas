package org.apereo.cas.notifications.sms;

import org.apereo.cas.configuration.model.support.sms.RestfulSmsProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.hc.core5.http.HttpResponse;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.HashMap;

/**
 * This is {@link RestfulSmsSender}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public record RestfulSmsSender(RestfulSmsProperties restProperties) implements SmsSender {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Override
    public boolean send(final String from, final String to, final String message) {
        HttpResponse response = null;
        try {
            val parameters = new HashMap<String, String>();
            val holder = ClientInfoHolder.getClientInfo();
            if (holder != null) {
                parameters.put("clientIpAddress", holder.getClientIpAddress());
                parameters.put("serverIpAddress", holder.getServerIpAddress());
            }
            parameters.put("from", from);
            parameters.put("to", to);

            val headers = CollectionUtils.<String, String>wrap("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            headers.putAll(restProperties.getHeaders());

            var exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .method(HttpMethod.valueOf(restProperties.getMethod().toUpperCase()))
                .url(restProperties.getUrl())
                .headers(headers);

            exec = switch (restProperties.getStyle()) {
                case QUERY_PARAMETERS -> exec.parameters(parameters).entity(message);
                case REQUEST_BODY -> {
                    parameters.put("text", message);
                    val body = FunctionUtils.doUnchecked(() -> MAPPER.writeValueAsString(parameters));
                    yield exec.entity(body);
                }
            };
            response = HttpUtils.execute(exec.build());
            return response != null && HttpStatus.valueOf(response.getCode()).is2xxSuccessful();
        } finally {
            HttpUtils.close(response);
        }
    }
}
