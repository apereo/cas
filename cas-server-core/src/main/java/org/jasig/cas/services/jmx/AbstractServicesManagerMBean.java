package org.jasig.cas.services.jmx;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceImpl;
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
 * @version $Revision$ $Date$
 * @since 3.4.4
 */
public abstract class AbstractServicesManagerMBean<T extends ServicesManager> {

    @NotNull
    private T servicesManager;

    protected AbstractServicesManagerMBean(final T servicesManager) {
        this.servicesManager = servicesManager;
    }

    protected final T getServicesManager() {
        return this.servicesManager;
    }

    @ManagedAttribute(description = "Retrieves the list of Registered Services in a slightly friendlier output.")
    public final List<String> getRegisteredServicesAsStrings() {
        final List<String> services = new ArrayList<String>();

        for (final RegisteredService r : this.servicesManager.getAllServices()) {
        services.add(new StringBuilder().append("id: ").append(r.getId())
                .append(" name: ").append(r.getName())
                .append(" enabled: ").append(r.isEnabled())
                .append(" ssoEnabled: ").append(r.isSsoEnabled())
                .append(" serviceId: ").append(r.getServiceId())
                .toString());
        }

        return services;
    }

    @ManagedOperation(description = "Can remove a service based on its identifier.")
    @ManagedOperationParameter(name="id", description = "the identifier to remove")
    public final RegisteredService removeService(final long id) {
        return this.servicesManager.delete(id);
    }

    @ManagedOperation(description = "Disable a service by id.")
    @ManagedOperationParameter(name="id", description = "the identifier to disable")
    public final void disableService(final long id) {
        changeEnabledState(id, false);
    }

    @ManagedOperation(description = "Enable a service by its id.")
    @ManagedOperationParameter(name="id", description = "the identifier to enable.")
    public final void enableService(final long id) {
        changeEnabledState(id, true);
    }

    private void changeEnabledState(final long id, final boolean newState) {
        final RegisteredService r = this.servicesManager.findServiceBy(id);
        Assert.notNull(r, "invalid RegisteredService id");

        // we screwed up our APIs in older versions of CAS, so we need to CAST this to do anything useful.
        ((RegisteredServiceImpl) r).setEnabled(newState);
        this.servicesManager.save(r);
    }
}
