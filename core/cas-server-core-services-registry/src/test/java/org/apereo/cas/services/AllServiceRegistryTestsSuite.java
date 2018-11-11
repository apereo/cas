package org.apereo.cas.services;

import org.apereo.cas.services.resource.CreateResourceBasedRegisteredServiceWatcherTests;
import org.apereo.cas.services.resource.DeleteResourceBasedRegisteredServiceWatcherTests;
import org.apereo.cas.services.resource.ModifyResourceBasedRegisteredServiceWatcherTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllServiceRegistryTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ServiceRegistryInitializerTests.class,
    DeleteResourceBasedRegisteredServiceWatcherTests.class,
    CreateResourceBasedRegisteredServiceWatcherTests.class,
    ModifyResourceBasedRegisteredServiceWatcherTests.class
})
public class AllServiceRegistryTestsSuite {
}
