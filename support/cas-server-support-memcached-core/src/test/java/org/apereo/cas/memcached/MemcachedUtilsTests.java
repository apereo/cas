package org.apereo.cas.memcached;

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
 */
@Tag("Memcached")
public class MemcachedUtilsTests {

    @Test
    public void verifySerial() {
        val props = new BaseMemcachedProperties();
        props.setTranscoder(BaseMemcachedProperties.TranscoderTypes.SERIAL);
        val coder = MemcachedUtils.newTranscoder(props);
        assertNotNull(coder);
    }

    @Test
    public void verifyWhalin() {
        val props = new BaseMemcachedProperties();
        props.setTranscoder(BaseMemcachedProperties.TranscoderTypes.WHALIN);
        val coder = MemcachedUtils.newTranscoder(props);
        assertNotNull(coder);
    }

    @Test
    public void verifyWhalinv1() {
        val props = new BaseMemcachedProperties();
        props.setTranscoder(BaseMemcachedProperties.TranscoderTypes.WHALINV1);
        val coder = MemcachedUtils.newTranscoder(props);
        assertNotNull(coder);
    }

    @Test
    public void verifyKryo() {
        val props = new BaseMemcachedProperties();
        props.setTranscoder(BaseMemcachedProperties.TranscoderTypes.KRYO);
        val coder = MemcachedUtils.newTranscoder(props);
        assertNotNull(coder);
    }
}
