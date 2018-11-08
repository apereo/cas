package org.apereo.cas.services;

import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Default In Memory Service Registry Dao for test/demonstration purposes.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@ToString
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InMemoryServiceRegistry extends AbstractServiceRegistry {

    private List<RegisteredService> registeredServices = new ArrayList<>();

    @Override
    public boolean delete(final RegisteredService registeredService) {
        return this.registeredServices.remove(registeredService);
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        return this.registeredServices.stream().filter(r -> r.getId() == id).findFirst().orElse(null);
    }

    @Override
    public RegisteredService findServiceById(final String id) {
        return this.registeredServices.stream().filter(r -> r.matches(id)).findFirst().orElse(null);
    }

    @Override
    public Collection<RegisteredService> load() {
        val services = new ArrayList<RegisteredService>();
        this.registeredServices.forEach(s -> {
            publishEvent(new CasRegisteredServiceLoadedEvent(this, s));
            services.add(s);
        });
        return services;
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        if (registeredService.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE) {
            registeredService.setId(findHighestId() + 1);
        }
        val svc = findServiceById(registeredService.getId());
        if (svc != null) {
            this.registeredServices.remove(svc);
        }
        this.registeredServices.add(registeredService);
        return registeredService;
    }

    /**
     * This isn't super-fast but we don't expect thousands of services.
     *
     * @return the highest service id in the list of registered services
     */
    private long findHighestId() {
        return this.registeredServices.stream().map(RegisteredService::getId).max(Comparator.naturalOrder()).orElse(0L);
    }

    @Override
    public long size() {
        return registeredServices.size();
    }
}
