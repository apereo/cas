package org.apereo.cas;

import org.apereo.cas.adaptors.fortress.FortressAuthenticationHandlerTests;
import org.apereo.cas.config.FortressAuthenticationConfigurationTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link CasFortressTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    FortressAuthenticationHandlerTests.class,
    FortressAuthenticationConfigurationTests.class
})
@RunWith(JUnitPlatform.class)
public class CasFortressTestsSuite {
}
