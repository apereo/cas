package org.jasig.cas.services.jmx;

import org.jasig.cas.services.ServicesManager;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * Supports the basic {@link org.jasig.cas.services.ServicesManager}.
 *
 * @author Scott Battaglia
 * @since 3.4.4
 */

@ManagedResource(objectName = "CAS:name=JasigCasServicesManagerMBean",
        description = "Exposes the services management tool via JMX", log = true, logFile="jasig_cas_jmx.logger",
        currencyTimeLimit = 15)
public final class ServicesManagerMBean extends AbstractServicesManagerMBean<ServicesManager> {

    /**
     * Instantiates a new services manager m bean.
     *
     * @param servicesManager the services manager
     */
    public ServicesManagerMBean(final ServicesManager servicesManager) {
        super(servicesManager);
    }
}
