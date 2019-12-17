package org.apereo.cas.web.flow.executor;

import com.google.common.base.Splitter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cryptacular.util.CodecUtil;
import org.springframework.util.Assert;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.repository.BadlyFormattedFlowExecutionKeyException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

/**
 * Spring Webflow execution id that contains the serialized flow execution state as part of the identifier.
 * Keys produced by this class have the form ID_BASE64 where ID is a globally unique identifier and BASE64
 * is the base-64 encoded bytes of a serialized object output stream.
 *
 * @author Marvin S. Addison
 * @since 6.1
 */
@EqualsAndHashCode(callSuper = false)
@Getter
@Slf4j
public class ClientFlowExecutionKey extends FlowExecutionKey {

    private static final String KEY_FORMAT = "<uuid>_<base64-encoded-flow-state>";

    private static final long serialVersionUID = 3514659327458916297L;

    private UUID id;

    private byte[] data;

    public ClientFlowExecutionKey(final byte[] data) {
        this(UUID.randomUUID(), data);
    }

    public ClientFlowExecutionKey(final UUID id, final byte[] data) {
        Assert.notNull(id, "Flow execution id cannot be null.");
        this.id = id;
        this.data = data;
    }

    public static ClientFlowExecutionKey parse(final String key) throws BadlyFormattedFlowExecutionKeyException {
        val tokens = Splitter.on('_').splitToList(key);
        if (tokens.size() != 2) {
            throw new BadlyFormattedFlowExecutionKeyException(key, KEY_FORMAT);
        }
        try {
            val uuid = UUID.fromString(tokens.get(0));
            val decoded = CodecUtil.b64(tokens.get(1));
            return new ClientFlowExecutionKey(uuid, decoded);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new BadlyFormattedFlowExecutionKeyException(key, KEY_FORMAT);
        }
    }
    
    @Override
    public String toString() {
        return this.id + "_" + CodecUtil.b64(this.data);
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeUTF(toString());
    }

    private void readObject(final ObjectInputStream in) throws Exception {
        val temp = parse(in.readUTF());
        this.id = temp.id;
        this.data = temp.data;
    }
}
