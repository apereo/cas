package org.apereo.cas.adaptors.duo;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link AllDuoSecurityTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */

@SelectClasses({
    DefaultDuoMultifactorAuthenticationProviderTests.class,
    DuoHealthIndicatorTests.class
})
public class AllDuoSecurityTestsSuite {
}
