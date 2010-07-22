/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.util;

import org.jasig.cas.ticket.Ticket;
import org.jboss.cache.Cache;
import org.jboss.cache.CacheFactory;
import org.jboss.cache.DefaultCacheFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.5
 *
 */
public final class JBossCacheFactoryBean implements FactoryBean, InitializingBean, DisposableBean {
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private Cache<String, Ticket> cache;
    
    private Resource configLocation;

    public Object getObject() throws Exception {
        return this.cache;
    }

    public Class<Cache> getObjectType() {
        return Cache.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        final CacheFactory<String, Ticket> cf = new DefaultCacheFactory<String, Ticket>();
        this.cache = cf.createCache(this.configLocation.getInputStream());
    }

    @Required
    public void setConfigLocation(final Resource configLocation) {
        this.configLocation = configLocation;
    }

    public void destroy() throws Exception {
        log.info("Shutting down TreeCache service.");
        this.cache.destroy();
    }
}
