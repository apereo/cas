package org.apereo.cas.adaptors.u2f;

import org.apereo.cas.adaptors.u2f.storage.MariaDbU2FJpaDeviceRepositoryTests;
import org.apereo.cas.adaptors.u2f.storage.MySQLU2FJpaDeviceRepositoryTests;
import org.apereo.cas.adaptors.u2f.storage.U2FJpaDeviceRepositoryTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * The {@link org.apereo.cas.AllTestsSuite} is responsible for
 * running all cas test cases.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses({
    MySQLU2FJpaDeviceRepositoryTests.class,
    MariaDbU2FJpaDeviceRepositoryTests.class,
    U2FJpaDeviceRepositoryTests.class
})
@Suite
public class AllTestsSuite {
}

