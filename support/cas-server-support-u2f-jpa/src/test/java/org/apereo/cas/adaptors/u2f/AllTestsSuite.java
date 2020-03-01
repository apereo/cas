package org.apereo.cas.adaptors.u2f;

import org.apereo.cas.adaptors.u2f.storage.MySQLU2FJpaDeviceRepositoryTests;
import org.apereo.cas.adaptors.u2f.storage.U2FJpaDeviceRepositoryTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * The {@link org.apereo.cas.AllTestsSuite} is responsible for
 * running all cas test cases.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses({
    MySQLU2FJpaDeviceRepositoryTests.class,
    U2FJpaDeviceRepositoryTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}

