package org.apereo.cas;

import org.apereo.cas.support.wsfederation.WsFederationAttributeMutatorTests;
import org.apereo.cas.support.wsfederation.WsFederationHelperTests;
import org.apereo.cas.support.wsfederation.attributes.GroovyWsFederationAttributeMutatorTests;
import org.apereo.cas.support.wsfederation.authentication.handler.support.WsFederationAuthenticationHandlerTests;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredentialTests;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredentialsToPrincipalResolverAllResolutionTests;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredentialsToPrincipalResolverCasResolutionTests;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredentialsToPrincipalResolverTests;
import org.apereo.cas.support.wsfederation.web.WsFederationCookieManagerTests;
import org.apereo.cas.support.wsfederation.web.WsFederationNavigationControllerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Test suite to run all tests.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses({
    WsFederationCredentialsToPrincipalResolverAllResolutionTests.class,
    WsFederationCredentialsToPrincipalResolverCasResolutionTests.class,
    WsFederationHelperTests.class,
    WsFederationAuthenticationHandlerTests.class,
    WsFederationNavigationControllerTests.class,
    WsFederationCookieManagerTests.class,
    GroovyWsFederationAttributeMutatorTests.class,
    WsFederationCredentialsToPrincipalResolverTests.class,
    WsFederationAttributeMutatorTests.class,
    WsFederationCredentialTests.class
})
@Suite
public class AllWsFederationTestsSuite {
}
