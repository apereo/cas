package org.apereo.cas;

import org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepositoryTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * The {@link AllCoreTestsSuite} is responsible for
 * running all cas test cases.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses({
    DefaultCentralAuthenticationServiceTests.class,
    DefaultCentralAuthenticationServiceMockitoTests.class,
    DefaultCasAttributeEncoderTests.class,
    DefaultPrincipalAttributesRepositoryTests.class,
    CachingPrincipalAttributesRepositoryTests.class
})
@Suite
public class AllCoreTestsSuite {
}
