package org.apereo.cas.memcached;

import module java.base;
import org.apereo.cas.configuration.model.support.memcached.BaseMemcachedProperties;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MemcachedPooledClientConnectionFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 * @deprecated Since 7.0.0
 */
@Tag("Memcached")
@EnabledIfListeningOnPort(port = 11211)
@Deprecated(since = "7.0.0")
class MemcachedPooledClientConnectionFactoryTests {

    @Test
    void verifyOperation() {
        val memcached = new BaseMemcachedProperties();
        memcached.setOpTimeout(10);
        memcached.setMaxReconnectDelay(10);
        memcached.setShutdownTimeoutSeconds(1);
        val factory = new MemcachedPooledClientConnectionFactory(memcached,
            MemcachedUtils.newTranscoder(memcached));
        val pool = factory.getObjectPool();
        assertDoesNotThrow(() -> {
            val client = pool.borrowObject();
            val object = factory.wrap(client);
            factory.destroyObject(object);
        });
    }
}
