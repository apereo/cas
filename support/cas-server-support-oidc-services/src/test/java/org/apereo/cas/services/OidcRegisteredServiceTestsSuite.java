package org.apereo.cas.services;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link OidcRegisteredServiceTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@SelectClasses({
    OidcRegisteredServiceTests.class,
    PairwiseOidcRegisteredServiceUsernameAttributeProviderTests.class
})
@Suite
public class OidcRegisteredServiceTestsSuite {
}
