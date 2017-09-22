package org.apereo.cas;

import org.apereo.cas.adaptors.generic.FileAuthenticationHandlerTests;
import org.apereo.cas.adaptors.generic.RejectUsersAuthenticationHandlerTests;
import org.apereo.cas.adaptors.generic.ShiroAuthenticationHandlerTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({FileAuthenticationHandlerTests.class, 
        RejectUsersAuthenticationHandlerTests.class,
        ShiroAuthenticationHandlerTests.class})
public class AllTestsSuite {
}
