/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.support;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServiceRegistryReloader;
import org.jasig.cas.services.ServiceRegistryManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;

/**
 * Implementation of the ServiceRegistryReloader that will refresh a Spring
 * context if the underlying file changes.
 * <p>
 * The algorithm is as follows. Check the last recorded lastModified time
 * against the current lastModified time. If they differ, reload the registry by
 * re-reading the context, deleting all of the current services and replacing
 * them with fresh copies from the file. This class locks on the
 * ServiceRegistry.
 * </p>
 * 
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @version $Revision$ $Date$
 * @since 3.0
 * @see org.jasig.cas.services.ServiceRegistryReloader
 */
public final class SpringApplicationContextServiceRegistryReloader implements
    ServiceRegistryReloader, InitializingBean, ResourceLoaderAware {

    /** The default file name for our services file. */
    private static final String DEFAULT_FILE_NAME = "services.xml";

    /** The log instance. */
    private final Log log = LogFactory.getLog(this.getClass());

    /** Allows you to add services to the registry. */
    private ServiceRegistryManager serviceRegistryManager;

    /** The actual file handle to the file. */
    private File serviceRegistryFile;

    /** The filename, if not the default. */
    private String fileName;

    /** The last time the file was modified. */
    private long timeLastModified = 0L;

    /** The resourceloader to find the file. */
    private ResourceLoader resourceLoader;

    /** The application context which stores the services. */
    private ClassPathXmlApplicationContext applicationContext;

    public void reloadServiceRegistry() {
        if (log.isDebugEnabled()) {
            log.info("Checking if service list changed since last reload.");
        }
        final long currentTimeLastModified = this.serviceRegistryFile
            .lastModified();

        if (this.timeLastModified != currentTimeLastModified) {
            this.timeLastModified = currentTimeLastModified;

            if (log.isInfoEnabled()) {
                log
                    .info("Last modified time changed on "
                        + this.serviceRegistryFile.getName()
                        + ".  File most likely modified.  Regenerating ServiceRegistry.");
            }

            synchronized (this.serviceRegistryManager) {

                this.applicationContext.refresh();

                if (log.isDebugEnabled()) {
                    log.debug("Clearing out previous ServiceRegistry entries.");
                }
                this.serviceRegistryManager.clear();

                for (final Iterator iter = this.applicationContext
                    .getBeansOfType(RegisteredService.class).values()
                    .iterator(); iter.hasNext();) {
                    final RegisteredService authenticatedService = (RegisteredService) iter
                        .next();
                    if (log.isDebugEnabled()) {
                        log.debug("Adding [" + authenticatedService.getId()
                            + "] to ServiceRegistry");
                    }
                    this.serviceRegistryManager
                        .addService(authenticatedService);
                }
            }
        }
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.serviceRegistryManager,
            "serviceRegistryManager is a required property.");

        if (this.fileName == null) {
            log.info("No fileName provided for " + this.getClass().getName()
                + ".  Using default file name: " + DEFAULT_FILE_NAME);
            this.fileName = DEFAULT_FILE_NAME;
        }

        this.serviceRegistryFile = this.resourceLoader.getResource(
            ResourceLoader.CLASSPATH_URL_PREFIX + this.fileName).getFile();

        this.applicationContext = new ClassPathXmlApplicationContext(
            this.fileName);

    }

    /**
     * Method to set the file name.
     * 
     * @param fileName the File name to set.
     */
    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    /**
     * Method to set the Service Registry Manager.
     * 
     * @param serviceRegistryManager the serviceRegistryManager to set.
     */
    public void setServiceRegistryManager(
        final ServiceRegistryManager serviceRegistryManager) {
        this.serviceRegistryManager = serviceRegistryManager;
    }

    /**
     * @param resourceLoader The resourceLoader to set.
     */
    public void setResourceLoader(final ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
