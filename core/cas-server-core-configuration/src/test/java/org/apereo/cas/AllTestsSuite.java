
package org.apereo.cas;

import org.apereo.cas.configuration.CasConfigurationPropertiesValidatorTests;
import org.apereo.cas.configuration.CasConfigurationWatchServiceTests;
import org.apereo.cas.configuration.support.CasConfigurationJasyptCipherExecutorTests;
import org.apereo.cas.configuration.support.DefaultCasConfigurationPropertiesSourceLocatorTests;

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
    CasConfigurationJasyptCipherExecutorTests.class,
    CasConfigurationPropertiesValidatorTests.class,
    CasConfigurationWatchServiceTests.class,
    DefaultCasConfigurationPropertiesSourceLocatorTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
