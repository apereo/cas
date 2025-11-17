package org.apereo.cas.ticket.registry.pubsub;

import org.apereo.cas.ticket.registry.pubsub.commands.BaseMessageQueueCommand;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.serialization.SerializationUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.core.serializer.Deserializer;
import org.springframework.core.serializer.Serializer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * This is {@link MessageQueueMessageSerializationHandler}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class MessageQueueMessageSerializationHandler implements Serializer<@NonNull Object>, Deserializer<@NonNull Object> {
    private final CipherExecutor cipher;

    @Override
    public void serialize(
        @NonNull
        final Object object, final OutputStream outputStream) throws IOException {
        val result = serializeToByteArray(object);
        outputStream.write(result);
        outputStream.flush();
    }

    @Override
    public byte @NonNull [] serializeToByteArray(@NonNull final Object object) {
        return SerializationUtils.serializeAndEncodeObject(cipher, (Serializable) object);
    }

    @NonNull
    @Override
    public BaseMessageQueueCommand deserialize(final InputStream inputStream) throws IOException {
        try (inputStream) {
            return deserializeFromByteArray(inputStream.readAllBytes());
        }
    }

    @NonNull
    @Override
    public BaseMessageQueueCommand deserializeFromByteArray(final byte @NonNull [] serialized) {
        return SerializationUtils.decodeAndDeserializeObject(serialized, cipher, BaseMessageQueueCommand.class);
    }
}
