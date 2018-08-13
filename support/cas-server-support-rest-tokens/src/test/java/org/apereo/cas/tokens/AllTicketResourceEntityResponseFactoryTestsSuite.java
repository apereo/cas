package org.apereo.cas.tokens;

import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllTicketResourceEntityResponseFactoryTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(Enclosed.class)
@Suite.SuiteClasses({
    JWTServiceTicketResourceEntityResponseFactoryTests.class,
    JWTTicketGrantingTicketResourceEntityResponseFactoryTests.class
})
public class AllTicketResourceEntityResponseFactoryTestsSuite {
}
