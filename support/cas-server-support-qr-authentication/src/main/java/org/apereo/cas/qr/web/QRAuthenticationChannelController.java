package org.apereo.cas.qr.web;

import org.apereo.cas.qr.validation.QRAuthenticationTokenValidatorService;
import org.apereo.cas.token.TokenConstants;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.messaging.Message;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
    /**
     * Topic name for QR authentication.
     */
    public static final String QR_SIMPLE_BROKER_DESTINATION_PREFIX = "/qrtopic";

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private static final String QR_AUTHENTICATION_CHANNEL_ID = "QR_AUTHENTICATION_CHANNEL_ID";

    private final MessageSendingOperations<String> messageTemplate;

    private final QRAuthenticationTokenValidatorService tokenValidatorService;

    @MessageMapping("/accept")
    @SneakyThrows
    public void verify(final Message<String> message) {
        val payload = message.getPayload();
        LOGGER.trace("Received payload [{}]", payload);
        val nativeHeaders = Objects.requireNonNull(message.getHeaders().get("nativeHeaders", LinkedMultiValueMap.class));
        if (!nativeHeaders.containsKey(QR_AUTHENTICATION_CHANNEL_ID)) {
            LOGGER.warn("Unable to locate [{}] in the message header", QR_AUTHENTICATION_CHANNEL_ID);
        } else {
            val channelId = Objects.requireNonNull(nativeHeaders.get(QR_AUTHENTICATION_CHANNEL_ID)).get(0);
            LOGGER.debug("Current channel id is [{}]", channelId);
            val resultMap = MAPPER.readValue(payload, new TypeReference<Map<String, String>>() {
            });
            val token = resultMap.get(TokenConstants.PARAMETER_NAME_TOKEN);
            val result = tokenValidatorService.validate(Optional.empty(), token);
            if (result.isSuccess()) {
                val toSend = String.format("%s/%s/verify", QR_SIMPLE_BROKER_DESTINATION_PREFIX, channelId);
                messageTemplate.convertAndSend(toSend,
                    Map.of("success", Boolean.TRUE.toString(), TokenConstants.PARAMETER_NAME_TOKEN, token));
            }
        }
    }
}
