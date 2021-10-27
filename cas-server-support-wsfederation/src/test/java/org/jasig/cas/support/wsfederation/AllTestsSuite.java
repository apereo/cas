package org.jasig.cas.support.wsfederation;

import org.jasig.cas.support.wsfederation.authentication.principal.WsFederationCredentialTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite to run all tests.
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    WsFederationHelperTests.class,
    WsFederationAttributeMutatorTests.class,
    WsFederationCredentialTests.class
})
public class AllTestsSuite {
}
