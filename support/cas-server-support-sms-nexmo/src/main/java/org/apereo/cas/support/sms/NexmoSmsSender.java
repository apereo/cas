package org.apereo.cas.support.sms;

import org.apereo.cas.configuration.model.support.sms.NexmoProperties;
import org.apereo.cas.util.io.SmsSender;

import com.nexmo.client.NexmoClient;
import com.nexmo.client.sms.MessageStatus;
import com.nexmo.client.sms.messages.TextMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.stream.Collectors;

/**
 * This is {@link NexmoSmsSender}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@Getter
public class NexmoSmsSender implements SmsSender {

    private final NexmoClient nexmoClient;

    public NexmoSmsSender(final NexmoProperties nexmo) {
        val builder = new NexmoClient.Builder();
        this.nexmoClient = builder.apiKey(nexmo.getApiToken())
            .apiSecret(nexmo.getApiSecret())
            .signatureSecret(nexmo.getSignatureSecret()).build();
    }

    @Override
    public boolean send(final String from, final String to, final String message) {
        try {
            val textMessage = new TextMessage(from, to, message);
            val response = nexmoClient.getSmsClient().submitMessage(textMessage);
            if (response.getMessageCount() > 0) {
                val results = response.getMessages().stream()
                    .filter(res -> res.getStatus() != MessageStatus.OK)
                    .collect(Collectors.toList());
                if (results.isEmpty()) {
                    return true;
                }
                results.forEach(res -> LOGGER.error("Text message submission has failed: [{}]", res));
            } else {
                LOGGER.error("No text messages could be sent. Response [{}]", response);
            }
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return false;
    }
}
