package org.apereo.cas.memcached;

import net.spy.memcached.transcoders.SerializingTranscoder;
import net.spy.memcached.transcoders.Transcoder;
import net.spy.memcached.transcoders.WhalinTranscoder;
import net.spy.memcached.transcoders.WhalinV1Transcoder;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.support.memcached.BaseMemcachedProperties;
import org.apereo.cas.memcached.kryo.CasKryoPool;
import org.apereo.cas.memcached.kryo.CasKryoTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link MemcachedUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public final class MemcachedUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemcachedUtils.class);

    private MemcachedUtils() {
    }

    /**
     * New transcoder transcoder.
     *
     * @param memcachedProperties the memcached properties
     * @return the transcoder
     */
    public static Transcoder newTranscoder(final BaseMemcachedProperties memcachedProperties) {
        return newTranscoder(memcachedProperties, new ArrayList<>());
    }

    /**
     * New transcoder transcoder.
     *
     * @param memcachedProperties     the memcached properties
     * @param kryoSerializableClasses the kryo serializable classes
     * @return the transcoder
     */
    public static Transcoder newTranscoder(final BaseMemcachedProperties memcachedProperties,
                                           final Collection<Class> kryoSerializableClasses) {
        switch (StringUtils.trimToEmpty(memcachedProperties.getTranscoder()).toLowerCase()) {
            case "serial":
                final SerializingTranscoder serial = new SerializingTranscoder();
                serial.setCompressionThreshold(memcachedProperties.getTranscoderCompressionThreshold());
                LOGGER.debug("Creating memcached transcoder [{}]", serial.getClass().getName());
                return serial;
            case "whalin":
                final WhalinTranscoder whalin = new WhalinTranscoder();
                whalin.setCompressionThreshold(memcachedProperties.getTranscoderCompressionThreshold());
                LOGGER.debug("Creating memcached transcoder [{}]", whalin.getClass().getName());
                return whalin;
            case "whalinv1":
                final WhalinV1Transcoder whalinv1 = new WhalinV1Transcoder();
                whalinv1.setCompressionThreshold(memcachedProperties.getTranscoderCompressionThreshold());
                LOGGER.debug("Creating memcached transcoder [{}]", whalinv1.getClass().getName());
                return whalinv1;
            case "kryo":
            default:
                final CasKryoPool kryoPool = new CasKryoPool(kryoSerializableClasses, true,
                        memcachedProperties.isKryoRegistrationRequired(),
                        memcachedProperties.isKryoObjectsByReference(),
                        memcachedProperties.isKryoAutoReset());
                final CasKryoTranscoder kryo = new CasKryoTranscoder(kryoPool);
                LOGGER.debug("Creating memcached transcoder [{}]", kryo.getClass().getName());
                return kryo;
        }
    }
}
