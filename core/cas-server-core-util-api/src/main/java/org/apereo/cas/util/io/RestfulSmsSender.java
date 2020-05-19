package org.apereo.cas.util.io;

import org.apereo.cas.configuration.model.support.sms.SmsProvidersProperties;
import org.apereo.cas.util.HttpUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.http.HttpResponse;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.http.HttpStatus;

import java.util.HashMap;

/**
 * This is {@link RestfulSmsSender}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@RequiredArgsConstructor
@Slf4j
public class RestfulSmsSender implements SmsSender {
    private final SmsProvidersProperties.Rest restProperties;

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
            response = HttpUtils.executePost(restProperties.getUrl(),
                restProperties.getBasicAuthUsername(),
                restProperties.getBasicAuthPassword(),
                message,
                parameters);

            if (response != null) {
                val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
                return status.is2xxSuccessful();
            }
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        } finally {
            HttpUtils.close(response);
        }
        return false;
    }
}
