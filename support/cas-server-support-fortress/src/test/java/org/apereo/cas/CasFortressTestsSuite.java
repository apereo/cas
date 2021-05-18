package org.apereo.cas;

import org.apereo.cas.adaptors.fortress.FortressAuthenticationHandlerTests;
import org.apereo.cas.config.FortressAuthenticationConfigurationTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

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
@Suite
public class CasFortressTestsSuite {
}
