package org.apereo.cas.memcached;

import module java.base;
import org.apereo.cas.configuration.model.support.memcached.BaseMemcachedProperties;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MemcachedUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 * @deprecated Since 7.0.0
 */
@Tag("Memcached")
@Deprecated(since = "7.0.0")
class MemcachedUtilsTests {

    @Test
    void verifySerial() {
        val props = new BaseMemcachedProperties();
        props.setTranscoder(BaseMemcachedProperties.TranscoderTypes.SERIAL);
        val coder = MemcachedUtils.newTranscoder(props);
        assertNotNull(coder);
    }

    @Test
    void verifyWhalin() {
        val props = new BaseMemcachedProperties();
        props.setTranscoder(BaseMemcachedProperties.TranscoderTypes.WHALIN);
        val coder = MemcachedUtils.newTranscoder(props);
        assertNotNull(coder);
    }

    @Test
    void verifyWhalinv1() {
        val props = new BaseMemcachedProperties();
        props.setTranscoder(BaseMemcachedProperties.TranscoderTypes.WHALINV1);
        val coder = MemcachedUtils.newTranscoder(props);
        assertNotNull(coder);
    }

    @Test
    void verifyKryo() {
        val props = new BaseMemcachedProperties();
        props.setTranscoder(BaseMemcachedProperties.TranscoderTypes.KRYO);
        val coder = MemcachedUtils.newTranscoder(props);
        assertNotNull(coder);
    }
}
