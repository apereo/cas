package org.apereo.cas.memcached;

import com.esotericsoftware.kryo.Serializer;
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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link MemcachedPooledConnectionFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class MemcachedPooledConnectionFactory extends BasePooledObjectFactory<MemcachedClientIF> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemcachedPooledConnectionFactory.class);
    private final BaseMemcachedProperties memcachedProperties;
    private final Map<Class<?>, Serializer> kryoSerializerMap;

    public MemcachedPooledConnectionFactory(final BaseMemcachedProperties memcachedProperties) {
        this(memcachedProperties, new LinkedHashMap<>());
    }

    public MemcachedPooledConnectionFactory(final BaseMemcachedProperties memcachedProperties, final Map<Class<?>, Serializer> kryoSerializerMap) {
        this.memcachedProperties = memcachedProperties;
        this.kryoSerializerMap = kryoSerializerMap;
    }

    @Override
    public MemcachedClientIF create() throws Exception {
        try {
            final MemcachedClientFactoryBean factoryBean = new MemcachedClientFactoryBean();
            factoryBean.setServers(memcachedProperties.getServers());

            switch (StringUtils.trimToEmpty(memcachedProperties.getTranscoder()).toLowerCase()) {
                case "serial":
                    factoryBean.setTranscoder(new SerializingTranscoder());
                    break;
                case "whalin":
                    factoryBean.setTranscoder(new WhalinTranscoder());
                    break;
                case "whalinv1":
                    factoryBean.setTranscoder(new WhalinV1Transcoder());
                    break;
                case "kryo":
                default:
                    final CasKryoTranscoder kryo = new CasKryoTranscoder();
                    kryo.setSerializerMap(this.kryoSerializerMap);
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
