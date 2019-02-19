package org.apereo.cas.services;

import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author battags
 * @since 3.0.0
 */
public class DefaultServicesManagerByEnvironmentTests extends AbstractServicesManagerTests {
    @Override
    protected ServicesManager getServicesManagerInstance() {
        return new DefaultServicesManager(serviceRegistry, mock(ApplicationEventPublisher.class),
            CollectionUtils.wrapSet("prod1", "qa1"));
    }

    @Test
    public void verifyServiceByEnvironment() {
        val r = new RegexRegisteredService();
        r.setId(2000);
        r.setName(getClass().getSimpleName());
        r.setServiceId(getClass().getSimpleName());
        r.setEnvironments(CollectionUtils.wrapHashSet("dev1"));
        this.servicesManager.save(r);
        assertNull(this.servicesManager.findServiceBy(getClass().getSimpleName()));
        assertNull(this.servicesManager.findServiceBy(2000));
        r.setEnvironments(CollectionUtils.wrapHashSet("prod1"));
        this.servicesManager.save(r);
        assertNotNull(this.servicesManager.findServiceBy(2000));
    }
}
