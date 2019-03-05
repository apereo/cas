package org.apereo.cas;

import org.apereo.cas.adaptors.generic.FileAuthenticationHandlerTests;
import org.apereo.cas.adaptors.generic.JsonResourceAuthenticationHandlerTests;
import org.apereo.cas.adaptors.generic.RejectUsersAuthenticationHandlerTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SelectClasses({
    FileAuthenticationHandlerTests.class,
    RejectUsersAuthenticationHandlerTests.class,
    JsonResourceAuthenticationHandlerTests.class
})
public class AllTestsSuite {
}
