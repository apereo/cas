package org.apereo.cas;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.generic.FileAuthenticationHandlerTests;
import org.apereo.cas.adaptors.generic.RejectUsersAuthenticationHandlerTests;
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
        RejectUsersAuthenticationHandlerTests.class})
@Slf4j
public class AllTestsSuite {
}
