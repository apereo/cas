package org.apereo.cas.services;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

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
@RunWith(JUnitPlatform.class)
public class OidcRegisteredServiceTestsSuite {
}
