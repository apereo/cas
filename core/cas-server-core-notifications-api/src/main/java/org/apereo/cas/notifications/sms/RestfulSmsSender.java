package org.apereo.cas.notifications.sms;

import module java.base;
import org.apereo.cas.configuration.model.support.sms.RestfulSmsProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.hc.core5.http.HttpResponse;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link RestfulSmsSender}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
public class RestfulSmsSender implements SmsSender {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final RestfulSmsProperties restProperties;
    private final HttpClient httpClient;
    
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

            val headers = CollectionUtils.<String, String>wrap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            headers.putAll(restProperties.getHeaders());

            var exec = HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .method(HttpMethod.valueOf(restProperties.getMethod().toUpperCase(Locale.ENGLISH)))
                .url(restProperties.getUrl())
                .httpClient(this.httpClient)
                .headers(headers);

            exec = switch (restProperties.getStyle()) {
                case QUERY_PARAMETERS -> exec.parameters(parameters).entity(message);
                case REQUEST_BODY -> {
                    parameters.put("text", message);
                    val body = MAPPER.writeValueAsString(parameters);
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
