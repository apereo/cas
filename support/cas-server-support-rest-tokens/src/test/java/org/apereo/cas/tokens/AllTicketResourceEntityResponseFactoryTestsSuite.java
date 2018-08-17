package org.apereo.cas.tokens;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllTicketResourceEntityResponseFactoryTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    JWTServiceTicketResourceEntityResponseFactoryTests.class,
    JWTTicketGrantingTicketResourceEntityResponseFactoryTests.class
})
public class AllTicketResourceEntityResponseFactoryTestsSuite {
}
