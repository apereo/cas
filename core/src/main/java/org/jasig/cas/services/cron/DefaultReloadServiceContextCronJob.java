/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.services.cron;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.services.AuthenticatedService;
import org.jasig.cas.services.ServiceRegistryManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ResourceLoader;

/**
 * Class to reload the ServiceRegistry from an XML file based on the Spring Bean
 * Factory.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class DefaultReloadServiceContextCronJob implements
    ReloadServiceContextCronJob, InitializingBean, ResourceLoaderAware {

    private final static String DEFAULT_FILE_NAME = "services.xml";

    private final Log log = LogFactory.getLog(this.getClass());

    private ServiceRegistryManager serviceRegistryManager;

    private File serviceRegistryFile;

    private String fileName;

    private long timeLastModified = 0L;

    private ResourceLoader resourceLoader;

    public void reloadServiceRegistry() {
        log.info("Checking if service list changed since last reload.");
        final long currentTimeLastModified = this.serviceRegistryFile
            .lastModified();

        if (this.timeLastModified != currentTimeLastModified) {
            this.timeLastModified = currentTimeLastModified;

            log
                .info("Last modified time changed on "
                    + this.serviceRegistryFile.getName()
                    + ".  File most likely modified.  Regenerating ServiceRegistry.");

            synchronized (this.serviceRegistryManager) {
                final ListableBeanFactory beanFactory = new ClassPathXmlApplicationContext(
                    this.fileName);

                log.debug("Clearing out previous ServiceRegistry entries.");
                this.serviceRegistryManager.clear();

                for (final Iterator iter = beanFactory.getBeansOfType(
                    AuthenticatedService.class).values().iterator(); iter
                    .hasNext();) {
                    final AuthenticatedService authenticatedService = (AuthenticatedService)iter
                        .next();
                    log.debug("Adding [" + authenticatedService.getId()
                        + "] to ServiceRegistry");
                    this.serviceRegistryManager
                        .addService(authenticatedService);
                }
            }
        }
    }

    public void afterPropertiesSet() throws Exception {
        if (this.serviceRegistryManager == null) {
            throw new IllegalStateException(
                "ServiceRegistryManager cannot be null on "
                    + this.getClass().getName());
        }

        if (this.fileName == null) {
            log.info("No fileName provided for " + this.getClass().getName()
                + ".  Using default file name: " + DEFAULT_FILE_NAME);
            this.fileName = DEFAULT_FILE_NAME;
        }

        this.serviceRegistryFile = this.resourceLoader.getResource(
            ResourceLoader.CLASSPATH_URL_PREFIX + this.fileName).getFile();

        log.info("LOCATION: " + this.serviceRegistryFile);

        if (!this.serviceRegistryFile.exists()) {
            throw new IllegalStateException("File: " + this.serviceRegistryFile
                + " does not exist.");
        }
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setServiceRegistryManager(
        ServiceRegistryManager serviceRegistryManager) {
        this.serviceRegistryManager = serviceRegistryManager;
    }

    /**
     * @param resourceLoader The resourceLoader to set.
     */
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

}
