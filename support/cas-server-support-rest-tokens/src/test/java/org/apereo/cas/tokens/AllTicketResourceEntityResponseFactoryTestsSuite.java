package org.apereo.cas.tokens;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link AllTicketResourceEntityResponseFactoryTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    JWTServiceTicketResourceEntityResponseFactoryTests.class,
    JWTTicketGrantingTicketResourceEntityResponseFactoryTests.class
})
public class AllTicketResourceEntityResponseFactoryTestsSuite {
}
