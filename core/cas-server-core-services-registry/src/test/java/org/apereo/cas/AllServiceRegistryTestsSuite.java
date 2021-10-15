package org.apereo.cas;

import org.apereo.cas.services.DefaultChainingServiceRegistryTests;
import org.apereo.cas.services.DefaultServiceRegistryInitializerEventListenerTests;
import org.apereo.cas.services.DefaultServiceRegistryInitializerTests;
import org.apereo.cas.services.replication.DefaultRegisteredServiceReplicationStrategyTests;
import org.apereo.cas.services.resource.CreateResourceBasedRegisteredServiceWatcherTests;
import org.apereo.cas.services.resource.DefaultRegisteredServiceResourceNamingStrategyTests;
import org.apereo.cas.services.resource.DeleteResourceBasedRegisteredServiceWatcherTests;
import org.apereo.cas.services.resource.ModifyResourceBasedRegisteredServiceWatcherTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllServiceRegistryTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    DefaultServiceRegistryInitializerTests.class,
    DefaultChainingServiceRegistryTests.class,
    DefaultRegisteredServiceReplicationStrategyTests.class,
    DefaultServiceRegistryInitializerEventListenerTests.class,
    DefaultRegisteredServiceResourceNamingStrategyTests.class,
    DeleteResourceBasedRegisteredServiceWatcherTests.class,
    CreateResourceBasedRegisteredServiceWatcherTests.class,
    ModifyResourceBasedRegisteredServiceWatcherTests.class
})
@Suite
public class AllServiceRegistryTestsSuite {
}
