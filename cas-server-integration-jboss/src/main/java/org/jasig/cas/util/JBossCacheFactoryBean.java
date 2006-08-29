/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.cache.PropertyConfigurator;
import org.jboss.cache.TreeCache;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.5
 *
 */
public final class JBossCacheFactoryBean implements FactoryBean, DisposableBean,
    InitializingBean {
    
    private Log log = LogFactory.getLog(this.getClass());
    
    private TreeCache cache;
    
    private Resource configLocation;

    public Object getObject() throws Exception {
        return this.cache;
    }

    public Class getObjectType() {
        return TreeCache.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void setConfigLocation(final Resource configLocation) {
        this.configLocation = configLocation;
    }

    public void destroy() throws Exception {
        log.info("Shutting down TreeCache service.");
        this.cache.stopService();
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.configLocation, "configLocation cannot be null.");
        this.cache = new TreeCache();
        final PropertyConfigurator propertyConfigurator = new PropertyConfigurator();
        propertyConfigurator.configure(this.cache, this.configLocation.getInputStream());
        log.info("Starting TreeCache service.");
        this.cache.startService();
    }
}
