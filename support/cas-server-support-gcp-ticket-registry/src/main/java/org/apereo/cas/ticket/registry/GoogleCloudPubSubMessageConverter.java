package org.apereo.cas.ticket.registry;

import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.support.converter.SimplePubSubMessageConverter;
import com.google.pubsub.v1.PubsubMessage;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * This is {@link GoogleCloudPubSubMessageConverter}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class GoogleCloudPubSubMessageConverter extends SimplePubSubMessageConverter {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private final CipherExecutor<byte[], byte[]> cipherExecutor;

    @Override
    public PubsubMessage toPubSubMessage(final Object payload, final Map<String, String> headers) {
        return FunctionUtils.doUnchecked(() -> {
            val serialized = MAPPER.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8);
            val convertedPayload = cipherExecutor.encode(serialized);
            return super.toPubSubMessage(convertedPayload, headers);
        });
    }

    @Override
    public <T> T fromPubSubMessage(final PubsubMessage message, final Class<T> payloadType) {
        return FunctionUtils.doUnchecked(() -> {
            val payload = new String(cipherExecutor.decode(message.getData().toByteArray()), StandardCharsets.UTF_8);
            return MAPPER.readValue(payload, payloadType);
        });

    }
}
