package org.apereo.cas;

import org.apereo.cas.adaptors.authy.AuthyMultifactorAuthenticationProviderTests;
import org.apereo.cas.adaptors.authy.DefaultAuthyClientInstanceTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllAuthyTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    DefaultAuthyClientInstanceTests.class,
    AuthyMultifactorAuthenticationProviderTests.class
})
@Suite
public class AllAuthyTestsSuite {
}
