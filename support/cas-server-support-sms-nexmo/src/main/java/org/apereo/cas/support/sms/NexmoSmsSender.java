package org.apereo.cas.support.sms;

import org.apereo.cas.util.io.SmsSender;

import com.nexmo.client.NexmoClient;
import com.nexmo.client.auth.AuthMethod;
import com.nexmo.client.auth.TokenAuthMethod;
import com.nexmo.client.sms.SmsSubmissionResult;
import com.nexmo.client.sms.messages.TextMessage;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This is {@link NexmoSmsSender}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class NexmoSmsSender implements SmsSender {
    private final AuthMethod authMethod;
    private final NexmoClient nexmoClient;

    public NexmoSmsSender(final String apiKey, final String apiSecret) {
        this.authMethod = new TokenAuthMethod(apiKey, apiSecret);
        this.nexmoClient = new NexmoClient(this.authMethod);
    }

    @Override
    public boolean send(final String from, final String to, final String message) {
        try {
            val textMessage = new TextMessage(from, to, message);
            val responses = nexmoClient.getSmsClient().submitMessage(textMessage);
            val results = Arrays.stream(responses)
                .filter(res -> res.getStatus() != SmsSubmissionResult.STATUS_OK)
                .collect(Collectors.toList());
            if (results.isEmpty()) {
                return true;
            }
            results.forEach(res -> LOGGER.error("Text message submission has failed: [{}]", res));
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
