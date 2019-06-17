package org.apereo.cas.adaptors.duo;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllDuoSecurityTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */

@SelectClasses({
    DefaultDuoSecurityMultifactorAuthenticationProviderTests.class,
    DuoSecurityHealthIndicatorTests.class
})
@RunWith(JUnitPlatform.class)
public class AllDuoSecurityTestsSuite {
}
