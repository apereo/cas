package org.apereo.cas.tokens;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTicketResourceEntityResponseFactoryTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    JwtServiceTicketResourceEntityResponseFactoryTests.class,
    JwtTicketGrantingTicketResourceEntityResponseFactoryTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTicketResourceEntityResponseFactoryTestsSuite {
}
