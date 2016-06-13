package org.apereo.cas.monitor.config;

import net.spy.memcached.MemcachedClientIF;
import org.apereo.cas.monitor.MemcachedMonitor;
import org.apereo.cas.monitor.Monitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nullable;

/**
 * This is {@link MemcachedMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("memcachedMonitorConfiguration")
public class MemcachedMonitorConfiguration {

    @Nullable
    @Autowired(required = false)
    @Qualifier("memcachedClient")
    private MemcachedClientIF memcachedClient;
    
    @Bean
    public Monitor memcachedMonitor() {
        final MemcachedMonitor m = new MemcachedMonitor();
        m.setMemcachedClient(memcachedClient);
        return m;
    }
}
