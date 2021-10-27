package org.jasig.cas.services.jmx;

import org.jasig.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class to support both the {@link org.jasig.cas.services.ServicesManager} and the
 * {@link org.jasig.cas.services.ReloadableServicesManager}.
 *
 * @author <a href="mailto:tobias.trelle@proximity.de">Tobias Trelle</a>
 * @author Scott Battaglia
 * @since 3.4.4
 */
public abstract class AbstractServicesManagerMBean<T extends ServicesManager> {

    @NotNull
    private final T servicesManager;

    /**
     * Instantiates a new abstract services manager m bean.
     *
     * @param svcMgr the svc mgr
     */
    protected AbstractServicesManagerMBean(final T svcMgr) {
        this.servicesManager = svcMgr;
    }

    protected final T getServicesManager() {
        return this.servicesManager;
    }

    /**
     * Gets the registered services as strings.
     *
     * @return the registered services as strings
     */
    @ManagedAttribute(description = "Retrieves the list of Registered Services in a slightly friendlier output.")
    public final List<String> getRegisteredServicesAsStrings() {
        final List<String> services = new ArrayList<>();

        for (final RegisteredService r : this.servicesManager.getAllServices()) {
        services.add(new StringBuilder().append("id: ").append(r.getId())
                .append(" name: ").append(r.getName())
                .append(" serviceId: ").append(r.getServiceId())
                .toString());
        }

        return services;
    }

    /**
     * Removes the service.
     *
     * @param id the id
     * @return the registered service
     */
    @ManagedOperation(description = "Can remove a service based on its identifier.")
    @ManagedOperationParameter(name="id", description = "the identifier to remove")
    public final RegisteredService removeService(final long id) {
        return this.servicesManager.delete(id);
    }

    /**
     * Disable service.
     *
     * @param id the id
     */
    @ManagedOperation(description = "Disable a service by id.")
    @ManagedOperationParameter(name="id", description = "the identifier to disable")
    public final void disableService(final long id) {
        changeEnabledState(id, false);
    }

    /**
     * Enable service.
     *
     * @param id the id
     */
    @ManagedOperation(description = "Enable a service by its id.")
    @ManagedOperationParameter(name="id", description = "the identifier to enable.")
    public final void enableService(final long id) {
        changeEnabledState(id, true);
    }

    /**
     * Change enabled state.
     *
     * @param id the id
     * @param newState the new state
     */
    private void changeEnabledState(final long id, final boolean newState) {
        final RegisteredService r = this.servicesManager.findServiceBy(id);
        Assert.notNull(r, "invalid RegisteredService id");

        // we screwed up our APIs in older versions of CAS, so we need to CAST this to do anything useful.
        ((DefaultRegisteredServiceAccessStrategy) r.getAccessStrategy()).setEnabled(newState);
        this.servicesManager.save(r);
    }
}
