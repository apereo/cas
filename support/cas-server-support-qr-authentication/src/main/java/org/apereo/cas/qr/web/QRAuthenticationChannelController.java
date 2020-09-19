package org.apereo.cas.qr.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.messaging.Message;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is {@link QRAuthenticationChannelController}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class QRAuthenticationChannelController {

    private final MessageSendingOperations<String> messageTemplate;

    @MessageMapping("/accept")
    public void verify(final Message<String> message) {
        LOGGER.debug("Received message [{}]", message.getPayload());
        val channelId = message.getHeaders()
            .get("nativeHeaders", LinkedMultiValueMap.class)
            .get("QR_AUTHENTICATION_CHANNEL_ID").get(0);
        val outcome = QRAuthenticationResult.builder().build();
        messageTemplate.convertAndSend(String.format("/%s/verify", channelId), outcome);
    }

    @Getter
    @SuperBuilder
    public static class QRAuthenticationResult {
        private final boolean success;
    }
}
