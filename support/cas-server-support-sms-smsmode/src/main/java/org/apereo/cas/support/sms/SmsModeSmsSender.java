package org.apereo.cas.support.sms;

import org.apereo.cas.configuration.model.support.sms.SmsModeProperties;
import org.apereo.cas.notifications.sms.SmsSender;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * This is {@link SmsModeSmsSender}.
 *
 * @author Jérôme Rautureau
 * @since 6.5.0
 */
@Slf4j
public record SmsModeSmsSender(SmsModeProperties properties) implements SmsSender {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Override
    public boolean send(final String from, final String to, final String message) {
        HttpResponse response = null;
        try {
            val data = new HashMap<String, Object>();
            val recipient = new HashMap<String, Object>();
            recipient.put("to", to);
            data.put("recipient", recipient);
            val body = new HashMap<String, Object>();
            body.put("text", message);
            data.put("body", body);
            data.put("from", from);

            val headers = CollectionUtils.<String, String>wrap(
                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE,
                HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE,
                "X-Api-Key", properties.getAccessToken());
            val exec = HttpExecutionRequest.builder()
                .method(HttpMethod.POST)
                .url(properties.getUrl())
                .proxyUrl(properties.getProxyUrl())
                .headers(headers)
                .entity(MAPPER.writeValueAsString(data))
                .build();
            response = HttpUtils.execute(exec);

            val status = HttpStatus.valueOf(response.getCode());
            try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                val entity = IOUtils.toString(content, StandardCharsets.UTF_8);
                LOGGER.debug("Response from SmsMode: [{}]", entity);
                return status.is2xxSuccessful();
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return false;
    }
}
