package org.apereo.cas;

import org.apereo.cas.model.CapacityTests;
import org.apereo.cas.util.serialization.StringSerializerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    CapacityTests.class,
    StringSerializerTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
