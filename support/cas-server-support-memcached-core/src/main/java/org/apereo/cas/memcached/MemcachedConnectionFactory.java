package org.apereo.cas.memcached;

import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.DefaultHashAlgorithm;
import net.spy.memcached.FailureMode;
import net.spy.memcached.MemcachedClientIF;
import net.spy.memcached.spring.MemcachedClientFactoryBean;
import net.spy.memcached.transcoders.SerializingTranscoder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apereo.cas.configuration.model.support.memcached.BaseMemcachedProperties;
import org.apereo.cas.memcached.kryo.KryoTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link MemcachedConnectionFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class MemcachedConnectionFactory extends BasePooledObjectFactory<MemcachedClientIF> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemcachedConnectionFactory.class);
    private final BaseMemcachedProperties memcachedProperties;

    public MemcachedConnectionFactory(final BaseMemcachedProperties memcachedProperties) {
        this.memcachedProperties = memcachedProperties;
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
                case "kryo":
                default:
                    factoryBean.setTranscoder(new KryoTranscoder());
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
}
