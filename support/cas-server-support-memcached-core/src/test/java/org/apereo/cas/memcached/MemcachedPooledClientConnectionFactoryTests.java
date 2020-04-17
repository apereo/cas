package org.apereo.cas.memcached;

import org.apereo.cas.configuration.model.support.memcached.BaseMemcachedProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MemcachedPooledClientConnectionFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Memcached")
@EnabledIfPortOpen(port = 11211)
public class MemcachedPooledClientConnectionFactoryTests {

    @Test
    public void verifyOperation() {
        val memcached = new BaseMemcachedProperties();
        memcached.setOpTimeout(10);
        memcached.setMaxReconnectDelay(10);
        memcached.setShutdownTimeoutSeconds(1);
        val factory = new MemcachedPooledClientConnectionFactory(memcached,
            MemcachedUtils.newTranscoder(memcached));
        val pool = factory.getObjectPool();
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                val client = pool.borrowObject();
                val object = factory.wrap(client);
                factory.destroyObject(object);
            }
        });
    }
}
