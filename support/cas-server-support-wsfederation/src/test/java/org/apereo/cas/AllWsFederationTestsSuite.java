package org.apereo.cas;

import org.apereo.cas.support.wsfederation.WsFederationAttributeMutatorTests;
import org.apereo.cas.support.wsfederation.WsFederationHelperTests;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredentialTests;
import org.apereo.cas.support.wsfederation.web.WsFederationCookieManagerTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * Test suite to run all tests.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses({
    WsFederationHelperTests.class,
    WsFederationCookieManagerTests.class,
    WsFederationAttributeMutatorTests.class,
    WsFederationCredentialTests.class
})
public class AllWsFederationTestsSuite {
}
