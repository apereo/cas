package org.apereo.cas.qr.web;

import org.apereo.cas.otp.util.QRUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.messaging.Message;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    @GetMapping(path = "/qr/channel")
    public void generate(final HttpServletResponse response, final HttpServletRequest request) throws Exception {
        response.setContentType("image/png");
        val id = request.getSession(true).getId();
        LOGGER.debug("Generating QR code with channel id [{}]", id);
        QRUtils.generateQRCode(response.getOutputStream(), id, QRUtils.WIDTH_LARGE, QRUtils.WIDTH_LARGE);
    }

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
