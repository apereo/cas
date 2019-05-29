package org.apereo.cas.adaptors.authy;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllAuthyTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    AuthyClientInstanceTests.class,
    AuthyMultifactorAuthenticationProviderTests.class
})
@RunWith(JUnitPlatform.class)
public class AllAuthyTestsSuite {
}
