/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
 * @deprecated As of 4.1 the Jboss cache integration module is no longer supported.
 * Please use other means of confguring your distributed ticket registry, such as
 * ehcache or memcached integrations with CAS.
 * @author Scott Battaglia
 * @since 3.0.0.5
 *
 */
@Deprecated
public final class JBossCacheFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Cache<String, Ticket> cache;

    private Resource configLocation;

    @Override
    public Object getObject() throws Exception {
        return this.cache;
    }

    @Override
    public Class<Cache> getObjectType() {
        return Cache.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final CacheFactory<String, Ticket> cf = new DefaultCacheFactory<>();
        this.cache = cf.createCache(this.configLocation.getInputStream());
    }

    @Required
    public void setConfigLocation(final Resource configLocation) {
        this.configLocation = configLocation;
    }

    @Override
    public void destroy() throws Exception {
        logger.info("Shutting down TreeCache service.");
        this.cache.destroy();
    }
}
