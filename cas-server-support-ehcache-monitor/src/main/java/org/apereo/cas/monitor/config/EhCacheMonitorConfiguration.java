package org.apereo.cas.monitor.config;

import net.sf.ehcache.Cache;
import org.apereo.cas.monitor.EhCacheMonitor;
import org.apereo.cas.monitor.Monitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link EhCacheMonitorConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("ehcacheMonitorConfiguration")
public class EhCacheMonitorConfiguration {

    @Autowired(required = false)
    @Qualifier("ehcacheTicketsCache")
    private Cache ehcacheTicketsCache;

    @Bean
    public Monitor ehcacheMonitor() {
        return new EhCacheMonitor(ehcacheTicketsCache);
    }
}
