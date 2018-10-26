package org.apereo.cas.services;

import org.apereo.cas.services.resource.CreateResourceBasedRegisteredServiceWatcherTests;
import org.apereo.cas.services.resource.DeleteResourceBasedRegisteredServiceWatcherTests;
import org.apereo.cas.services.resource.ModifyResourceBasedRegisteredServiceWatcherTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link AllServiceRegistryTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SelectClasses({
    ServiceRegistryInitializerTests.class,
    DeleteResourceBasedRegisteredServiceWatcherTests.class,
    CreateResourceBasedRegisteredServiceWatcherTests.class,
    ModifyResourceBasedRegisteredServiceWatcherTests.class
})
public class AllServiceRegistryTestsSuite {
}
