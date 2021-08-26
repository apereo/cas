package org.apereo.cas;

import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManagerTests;
import org.apereo.cas.configuration.CasConfigurationPropertiesTests;
import org.apereo.cas.configuration.CasCoreConfigurationUtilsTests;
import org.apereo.cas.configuration.CommaSeparatedStringToThrowablesConverterTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    CommaSeparatedStringToThrowablesConverterTests.class,
    CasConfigurationPropertiesTests.class,
    CasConfigurationPropertiesEnvironmentManagerTests.class,
    CasCoreConfigurationUtilsTests.class
})
@Suite
public class AllTestsSuite {
}
