package org.jasig.cas.services;

/**
 * Interface to allow for ServicesManagers to attempt to reload their list of
 * services.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public interface ReloadableServicesManager extends ServicesManager {

    /**
     * Inform the ServicesManager to reload its list of services if its cached
     * them. Note that this is a suggestion and that ServicesManagers are free
     * to reload whenever they want.
     */
    void reload();

}
