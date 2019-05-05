package org.apereo.cas.adaptors.authy;

import org.junit.platform.suite.api.SelectClasses;

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
public class AllAuthyTestsSuite {
}
