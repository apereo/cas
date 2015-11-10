package org.jasig.cas.services.jmx;

import org.jasig.cas.services.ReloadableServicesManager;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * Provides capabilities to reload a {@link org.jasig.cas.services.ReloadableServicesManager} from JMX.
 * <p>
 * You should only expose either this class or
 * the {@link org.jasig.cas.services.jmx.ServicesManagerMBean}, but not both.
 *
 * @author Scott Battaglia

 * @since 3.4.4
 */
@ManagedResource(objectName = "CAS:name=JasigCasServicesManagerMBean",
        description = "Exposes the services management tool via JMX", log = true, logFile="jasig_cas_jmx.logger",
        currencyTimeLimit = 15)
public final class ReloadableServicesManagerMBean extends AbstractServicesManagerMBean<ReloadableServicesManager> {

    /**
     * Instantiates a new reloadable services manager m bean.
     *
     * @param reloadableServicesManager the reloadable services manager
     */
    public ReloadableServicesManagerMBean(final ReloadableServicesManager reloadableServicesManager) {
        super(reloadableServicesManager);
    }

    /**
     * Reload services that are provided by the manager.
     */
    @ManagedOperation(description = "Reloads the list of the services from the persistence storage.")
    public void reload() {
        getServicesManager().reload();
    }
}
