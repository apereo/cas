package org.apereo.cas.notifications.sms;

import org.apereo.cas.configuration.model.support.sms.RestfulSmsProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.http.HttpResponse;
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
@Getter
@RequiredArgsConstructor
public class RestfulSmsSender implements SmsSender {
    private final RestfulSmsProperties restProperties;

    @Override
    public boolean send(final String from, final String to, final String message) {
        HttpResponse response = null;
        try {
            val parameters = new HashMap<String, Object>();
            val holder = ClientInfoHolder.getClientInfo();
            if (holder != null) {
                parameters.put("clientIpAddress", holder.getClientIpAddress());
                parameters.put("serverIpAddress", holder.getServerIpAddress());
            }
            parameters.put("from", from);
            parameters.put("to", to);

            val headers = CollectionUtils.<String, Object>wrap("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            headers.putAll(restProperties.getHeaders());
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .basicAuthPassword(restProperties.getBasicAuthPassword())
                .basicAuthUsername(restProperties.getBasicAuthUsername())
                .method(HttpMethod.POST)
                .url(restProperties.getUrl())
                .parameters(parameters)
                .entity(message)
                .headers(headers)
                .build();
            
            response = HttpUtils.execute(exec);
            if (response != null) {
                val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
                return status.is2xxSuccessful();
            }
        } finally {
            HttpUtils.close(response);
        }
        return false;
    }
}
