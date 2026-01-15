package org.apereo.cas.memcached.kryo;

import module java.base;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasKryoPoolTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Memcached")
@EnabledIfListeningOnPort(port = 11211)
class CasKryoPoolTests {
    @Test
    void verifyRunOperation() {
        val pool = new CasKryoPool();
        try (val kryo = pool.borrow()) {
            assertNotNull(kryo);
            pool.free(kryo);
        }
    }

}
