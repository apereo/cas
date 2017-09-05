package org.apereo.cas.ticket.registry;

import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.DefaultHashAlgorithm;
import net.spy.memcached.FailureMode;
import net.spy.memcached.MemcachedClientIF;
import net.spy.memcached.spring.MemcachedClientFactoryBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apereo.cas.configuration.model.support.memcached.MemcachedTicketRegistryProperties;
import org.apereo.cas.ticket.registry.support.kryo.KryoTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link MemcachedPooledConnectionFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class MemcachedPooledConnectionFactory extends BasePooledObjectFactory<MemcachedClientIF> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemcachedPooledConnectionFactory.class);
    private final MemcachedTicketRegistryProperties memcachedProperties;

    public MemcachedPooledConnectionFactory(final MemcachedTicketRegistryProperties memcachedProperties) {
        this.memcachedProperties = memcachedProperties;
    }

    @Override
    public MemcachedClientIF create() throws Exception {
        try {
            final MemcachedClientFactoryBean factoryBean = new MemcachedClientFactoryBean();
            factoryBean.setServers(memcachedProperties.getServers());
            factoryBean.setTranscoder(new KryoTranscoder());

            if (StringUtils.isNotBlank(memcachedProperties.getLocatorType())) {
                factoryBean.setLocatorType(ConnectionFactoryBuilder.Locator.valueOf(memcachedProperties.getLocatorType()));
            }
            if (StringUtils.isNotBlank(memcachedProperties.getFailureMode())) {
                factoryBean.setFailureMode(FailureMode.valueOf(memcachedProperties.getFailureMode()));
            }
            if (StringUtils.isNotBlank(memcachedProperties.getHashAlgorithm())) {
                factoryBean.setHashAlg(DefaultHashAlgorithm.valueOf(memcachedProperties.getHashAlgorithm()));
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
