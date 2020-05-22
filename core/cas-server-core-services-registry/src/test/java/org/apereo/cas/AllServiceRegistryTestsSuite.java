package org.apereo.cas;

import org.apereo.cas.services.ServiceRegistryInitializerEventListenerTests;
import org.apereo.cas.services.ServiceRegistryInitializerTests;
import org.apereo.cas.services.replication.DefaultRegisteredServiceReplicationStrategyTests;
import org.apereo.cas.services.resource.CreateResourceBasedRegisteredServiceWatcherTests;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategyTests;
import org.apereo.cas.services.resource.DeleteResourceBasedRegisteredServiceWatcherTests;
import org.apereo.cas.services.resource.ModifyResourceBasedRegisteredServiceWatcherTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllServiceRegistryTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    ServiceRegistryInitializerTests.class,
    DefaultRegisteredServiceReplicationStrategyTests.class,
    ServiceRegistryInitializerEventListenerTests.class,
    DefaultRegisteredServiceResourceNamingStrategyTests.class,
    DeleteResourceBasedRegisteredServiceWatcherTests.class,
    CreateResourceBasedRegisteredServiceWatcherTests.class,
    ModifyResourceBasedRegisteredServiceWatcherTests.class
})
@RunWith(JUnitPlatform.class)
public class AllServiceRegistryTestsSuite {
}
