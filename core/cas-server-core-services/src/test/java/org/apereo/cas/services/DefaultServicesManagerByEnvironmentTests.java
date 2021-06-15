package org.apereo.cas.services;

import org.apereo.cas.util.CollectionUtils;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author battags
 * @since 3.0.0
 */
@Tag("RegisteredService")
public class DefaultServicesManagerByEnvironmentTests extends AbstractServicesManagerTests<DefaultServicesManager> {
    @Override
    protected ServicesManager getServicesManagerInstance() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val context = ServicesManagerConfigurationContext.builder()
            .serviceRegistry(serviceRegistry)
            .applicationContext(applicationContext)
            .environments(CollectionUtils.wrapSet("prod1", "qa1"))
            .servicesCache(Caffeine.newBuilder().build())
            .registeredServiceLocators(List.of(new DefaultServicesManagerRegisteredServiceLocator()))
            .build();

        return new DefaultServicesManager(context);
    }

    @Test
    public void verifyServiceByEnvironment() {
        val r = new RegexRegisteredService();
        r.setId(2000);
        r.setName(getClass().getSimpleName());
        r.setServiceId(getClass().getSimpleName());
        r.setEnvironments(CollectionUtils.wrapHashSet("dev1"));
        servicesManager.save(r);
        assertNull(servicesManager.findServiceBy(serviceFactory.createService(getClass().getSimpleName())));
        assertNull(servicesManager.findServiceBy(2000));
        r.setEnvironments(CollectionUtils.wrapHashSet("prod1"));
        servicesManager.save(r);
        assertNotNull(servicesManager.findServiceBy(2000));
    }
}
