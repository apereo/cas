package org.apereo.cas.adaptors.authy;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

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
@Suite
public class AllAuthyTestsSuite {
}
