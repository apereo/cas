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
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.5
 *
 */
public final class JBossCacheFactoryBean implements FactoryBean, DisposableBean {
    
    private Log log = LogFactory.getLog(this.getClass());
    
    private TreeCache cache = new TreeCache();
    
    public JBossCacheFactoryBean() throws Exception {
        // nothing to do
    }

    public Object getObject() throws Exception {
        return this.cache;
    }

    public Class<TreeCache> getObjectType() {
        return TreeCache.class;
    }

    public boolean isSingleton() {
        return true;
    }

    @Required
    public void setConfigLocation(final Resource configLocation) {
        try {
            new PropertyConfigurator().configure(this.cache, configLocation.getInputStream());
            log.info("Starting TreeCache service.");
            this.cache.startService();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void destroy() throws Exception {
        log.info("Shutting down TreeCache service.");
        this.cache.stopService();
    }
}
