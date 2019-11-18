package org.apereo.cas.memcached;

import org.apereo.cas.configuration.model.support.memcached.BaseMemcachedProperties;
import org.apereo.cas.memcached.kryo.CasKryoPool;
import org.apereo.cas.memcached.kryo.CasKryoTranscoder;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.spy.memcached.transcoders.SerializingTranscoder;
import net.spy.memcached.transcoders.Transcoder;
import net.spy.memcached.transcoders.WhalinTranscoder;
import net.spy.memcached.transcoders.WhalinV1Transcoder;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link MemcachedUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@UtilityClass
public class MemcachedUtils {

    /**
     * New transcoder transcoder.
     *
     * @param memcachedProperties the memcached properties
     * @return the transcoder
     */
    public static Transcoder newTranscoder(final BaseMemcachedProperties memcachedProperties) {
        return newTranscoder(memcachedProperties, new ArrayList<>(0));
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
                val serial = new SerializingTranscoder();
                serial.setCompressionThreshold(memcachedProperties.getTranscoderCompressionThreshold());
                LOGGER.debug("Creating memcached transcoder [{}]", serial.getClass().getName());
                return serial;
            case "whalin":
                val whalin = new WhalinTranscoder();
                whalin.setCompressionThreshold(memcachedProperties.getTranscoderCompressionThreshold());
                LOGGER.debug("Creating memcached transcoder [{}]", whalin.getClass().getName());
                return whalin;
            case "whalinv1":
                val whalinv1 = new WhalinV1Transcoder();
                whalinv1.setCompressionThreshold(memcachedProperties.getTranscoderCompressionThreshold());
                LOGGER.debug("Creating memcached transcoder [{}]", whalinv1.getClass().getName());
                return whalinv1;
            case "kryo":
            default:
                val kryoPool = new CasKryoPool(kryoSerializableClasses, true,
                    memcachedProperties.isKryoRegistrationRequired(),
                    memcachedProperties.isKryoObjectsByReference(),
                    memcachedProperties.isKryoAutoReset());
                val kryo = new CasKryoTranscoder(kryoPool);
                LOGGER.debug("Creating memcached transcoder [{}]", kryo.getClass().getName());
                return kryo;
        }
    }
}
