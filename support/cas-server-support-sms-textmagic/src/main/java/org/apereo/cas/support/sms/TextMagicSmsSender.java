package org.apereo.cas.support.sms;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.io.SmsSender;

import com.textmagic.sdk.RestClient;
import com.textmagic.sdk.resource.instance.TMNewMessage;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link TextMagicSmsSender}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class TextMagicSmsSender implements SmsSender {
    private final RestClient client;

    public TextMagicSmsSender(final String uid, final String token) {
        client = new RestClient(uid, token);
    }

    public TextMagicSmsSender(final String uid, final String token,
                              final String url,
                              final HttpClient httpClient) {
        client = StringUtils.isNotBlank(url) ? new RestClient(uid, token, url) : new RestClient(uid, token);
        client.setHttpClient(httpClient.getWrappedHttpClient());
    }

    @Override
    public boolean send(final String from, final String to, final String message) {
        try {
            val m = this.client.getResource(TMNewMessage.class);
            m.setText(message);
            m.setPhones(CollectionUtils.wrap(to));
            m.setFrom(from);
            m.send();
            return true;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}


