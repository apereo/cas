package org.apereo.cas.web.flow.executor;

import module java.base;
import lombok.val;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.webflow.execution.repository.BadlyFormattedFlowExecutionKeyException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ClientFlowExecutionKeyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Webflow")
class ClientFlowExecutionKeyTests {

    @Test
    void verifySerialization() {
        val key = new ClientFlowExecutionKey(getClass().getSimpleName().getBytes(StandardCharsets.UTF_8));
        val result = SerializationUtils.serialize(key);
        assertNotNull(result);
        val newKey = SerializationUtils.deserialize(result);
        assertNotNull(newKey);
    }

    @Test
    void verifyBadKey() {
        assertThrows(BadlyFormattedFlowExecutionKeyException.class, () -> ClientFlowExecutionKey.parse("bad-key"));
        assertThrows(BadlyFormattedFlowExecutionKeyException.class, () -> ClientFlowExecutionKey.parse("bad_key"));

    }
}
