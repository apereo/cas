package org.jasig.cas.config;

import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.DefaultHashAlgorithm;
import net.spy.memcached.FailureMode;
import net.spy.memcached.spring.MemcachedClientFactoryBean;
import org.jasig.cas.ticket.registry.support.kryo.KryoTranscoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link MemcachedConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("memcachedConfiguration")
public class MemcachedConfiguration {

    /**
     * The Transcoder.
     */
    @Autowired
    @Qualifier("kryoTranscoder")
    private KryoTranscoder transcoder;

    /**
     * The Servers.
     */
    @Value("${memcached.servers:localhost:11211}")
    private String servers;

    /**
     * The Failure mode.
     */
    @Value("${memcached.failureMode:Redistribute}")
    private FailureMode failureMode;

    /**
     * The Locator type.
     */
    @Value("${memcached.locatorType:ARRAY_MOD}")
    private ConnectionFactoryBuilder.Locator locatorType;

    /**
     * The Hash algorithm.
     */
    @Value("net.spy.memcached.DefaultHashAlgorithm.${memcached.hashAlgorithm:FNV1_64_HASH}")
    private DefaultHashAlgorithm hashAlgorithm;

    /**
     * Memcached client memcached client factory bean.
     *
     * @return the memcached client factory bean
     */
    @RefreshScope
    @Bean(name = "memcachedClient")
    public MemcachedClientFactoryBean memcachedClient() {
        final MemcachedClientFactoryBean bean = new MemcachedClientFactoryBean();
        bean.setServers(this.servers);
        bean.setLocatorType(this.locatorType);
        bean.setTranscoder(this.transcoder);
        bean.setFailureMode(this.failureMode);
        bean.setHashAlg(this.hashAlgorithm);
        return bean;
    }
}
