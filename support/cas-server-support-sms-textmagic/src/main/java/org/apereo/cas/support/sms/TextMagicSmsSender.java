package org.apereo.cas.support.sms;

import com.textmagic.sdk.RestClient;
import com.textmagic.sdk.resource.instance.TMNewMessage;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.io.SmsSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link TextMagicSmsSender}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class TextMagicSmsSender implements SmsSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(TextMagicSmsSender.class);
    
    private final RestClient client;
            
    public TextMagicSmsSender(final String uid, final String token) {
        client = new RestClient(uid, token);
    }

    @Override
    public boolean send(final String from, final String to, final String message) {
        try {
            final TMNewMessage m = this.client.getResource(TMNewMessage.class);
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


