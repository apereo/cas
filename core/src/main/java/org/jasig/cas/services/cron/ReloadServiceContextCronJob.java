/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
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
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * Class to reload the ServiceRegistry from an XML file based on the Spring Bean Factory.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class ReloadServiceContextCronJob implements InitializingBean {
    
    private final static String DEFAULT_FILE_NAME = "services.xml";
    
    private final Log log = LogFactory.getLog(this.getClass());
    
    private ServiceRegistryManager serviceRegistryManager;
    
    private File serviceRegistryFile;
    
    private String fileName;
    
    private long timeLastModified = 0L;
    
    public synchronized void reloadServiceRegistry() {
        final long currentTimeLastModified = this.serviceRegistryFile.lastModified();
        
        if (this.timeLastModified != currentTimeLastModified) {
            this.timeLastModified = currentTimeLastModified;
            
            log.info("Last modified time changed on " + this.serviceRegistryFile.getName() + ".  File most likely modified.  Regenerating ServiceRegistry.");
            
            synchronized (this.serviceRegistryManager) {
                final ListableBeanFactory beanFactory = new ClassPathXmlApplicationContext(this.fileName);
                
                log.debug("Clearing out previous ServiceRegistry entries.");
                this.serviceRegistryManager.clear();
                
                for (final Iterator iter = beanFactory.getBeansOfType(AuthenticatedService.class).values().iterator(); iter.hasNext();) {
                    final AuthenticatedService authenticatedService = (AuthenticatedService) iter.next();
                    log.debug("Adding [" + authenticatedService.getId() + "] to ServiceRegistry");
                    this.serviceRegistryManager.addService(authenticatedService);
                }
            }
        }
    }
    
    public void afterPropertiesSet() throws Exception {
        if (this.fileName == null) {
            log.info("No fileName provided for " + this.getClass().getName() + ".  Using default file name: " + DEFAULT_FILE_NAME);
            this.fileName = DEFAULT_FILE_NAME;
        }
        
        this.serviceRegistryFile = new File(this.fileName);
    }
}
