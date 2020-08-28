package org.apereo.cas.memcached.kryo;

import org.apereo.cas.util.junit.EnabledIfPortOpen;

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
@EnabledIfPortOpen(port = 11211)
public class CasKryoPoolTests {
    @Test
    public void verifyRunOperation() {
        val input = new CasKryoPool();
        assertNotNull(input.run(kryo -> new Object()));
    }

}
