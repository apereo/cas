package org.apereo.cas.tokens;

import org.junit.platform.suite.api.SelectClasses;

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
public class AllTicketResourceEntityResponseFactoryTestsSuite {
}
