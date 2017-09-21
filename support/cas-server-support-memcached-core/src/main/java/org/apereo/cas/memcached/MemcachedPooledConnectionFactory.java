package org.apereo.cas.memcached;

import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.DefaultHashAlgorithm;
import net.spy.memcached.FailureMode;
import net.spy.memcached.MemcachedClientIF;
import net.spy.memcached.spring.MemcachedClientFactoryBean;
import net.spy.memcached.transcoders.SerializingTranscoder;
import net.spy.memcached.transcoders.WhalinTranscoder;
import net.spy.memcached.transcoders.WhalinV1Transcoder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apereo.cas.configuration.model.support.memcached.BaseMemcachedProperties;
import org.apereo.cas.memcached.kryo.CasKryoTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link MemcachedPooledConnectionFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class MemcachedPooledConnectionFactory extends BasePooledObjectFactory<MemcachedClientIF> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemcachedPooledConnectionFactory.class);
    private final BaseMemcachedProperties memcachedProperties;
    private final Collection<Class<?>> kryoSerializableClasses;

    public MemcachedPooledConnectionFactory(final BaseMemcachedProperties memcachedProperties) {
        this(memcachedProperties, new ArrayList<>());
    }

    public MemcachedPooledConnectionFactory(final BaseMemcachedProperties memcachedProperties, final Collection<Class<?>> kryoSerializableClasses) {
        this.memcachedProperties = memcachedProperties;
        this.kryoSerializableClasses = kryoSerializableClasses;
    }

    @Override
    public MemcachedClientIF create() throws Exception {
        try {
            final MemcachedClientFactoryBean factoryBean = new MemcachedClientFactoryBean();
            factoryBean.setServers(memcachedProperties.getServers());

            switch (StringUtils.trimToEmpty(memcachedProperties.getTranscoder()).toLowerCase()) {
                case "serial":
                    final SerializingTranscoder t = new SerializingTranscoder();
                    t.setCompressionThreshold(memcachedProperties.getTranscoderCompressionThreshold());
                    factoryBean.setTranscoder(t);
                    break;
                case "whalin":
                    final WhalinTranscoder t1 = new WhalinTranscoder();
                    t1.setCompressionThreshold(memcachedProperties.getTranscoderCompressionThreshold());
                    factoryBean.setTranscoder(t1);
                    break;
                case "whalinv1":
                    final WhalinV1Transcoder t2 = new WhalinV1Transcoder();
                    t2.setCompressionThreshold(memcachedProperties.getTranscoderCompressionThreshold());
                    factoryBean.setTranscoder(t2);
                    break;
                case "kryo":
                default:
                    final CasKryoTranscoder kryo = new CasKryoTranscoder(this.kryoSerializableClasses);
                    kryo.setAutoReset(memcachedProperties.isKryoAutoReset());
                    kryo.setRegistrationRequired(memcachedProperties.isKryoRegistrationRequired());
                    kryo.setReplaceObjectsByReferences(memcachedProperties.isKryoObjectsByReference());
                    kryo.initialize();
                    factoryBean.setTranscoder(kryo);
            }

            if (StringUtils.isNotBlank(memcachedProperties.getLocatorType())) {
                factoryBean.setLocatorType(ConnectionFactoryBuilder.Locator.valueOf(memcachedProperties.getLocatorType()));
            }
            if (StringUtils.isNotBlank(memcachedProperties.getFailureMode())) {
                factoryBean.setFailureMode(FailureMode.valueOf(memcachedProperties.getFailureMode()));
            }
            if (StringUtils.isNotBlank(memcachedProperties.getHashAlgorithm())) {
                factoryBean.setHashAlg(DefaultHashAlgorithm.valueOf(memcachedProperties.getHashAlgorithm()));
            }

            factoryBean.setDaemon(memcachedProperties.isDaemon());
            factoryBean.setShouldOptimize(memcachedProperties.isShouldOptimize());
            factoryBean.setUseNagleAlgorithm(memcachedProperties.isUseNagleAlgorithm());

            if (memcachedProperties.getOpTimeout() > 0) {
                factoryBean.setOpTimeout(memcachedProperties.getOpTimeout());
            }
            if (memcachedProperties.getMaxReconnectDelay() > 0) {
                factoryBean.setMaxReconnectDelay(memcachedProperties.getMaxReconnectDelay());
            }
            if (memcachedProperties.getShutdownTimeoutSeconds() > 0) {
                factoryBean.setShutdownTimeoutSeconds(memcachedProperties.getShutdownTimeoutSeconds());
            }
            if (memcachedProperties.getTimeoutExceptionThreshold() > 0) {
                factoryBean.setTimeoutExceptionThreshold(memcachedProperties.getTimeoutExceptionThreshold());
            }
            factoryBean.afterPropertiesSet();
            return (MemcachedClientIF) factoryBean.getObject();
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public PooledObject<MemcachedClientIF> wrap(final MemcachedClientIF memcachedClientIF) {
        return new DefaultPooledObject<>(memcachedClientIF);
    }

    @Override
    public void destroyObject(final PooledObject<MemcachedClientIF> p) throws Exception {
        try {
            p.getObject().shutdown();
            p.invalidate();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }


    /**
     * Gets object pool.
     *
     * @return the object pool
     */
    public ObjectPool<MemcachedClientIF> getObjectPool() {
        final GenericObjectPool<MemcachedClientIF> pool = new GenericObjectPool<>(this);
        pool.setMaxIdle(memcachedProperties.getMaxIdle());
        pool.setMinIdle(memcachedProperties.getMinIdle());
        pool.setMaxTotal(memcachedProperties.getMaxTotal());
        return pool;
    }
}
