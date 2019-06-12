package org.apereo.cas.support.rest;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@SelectClasses({
    TicketGrantingTicketResourceTests.class,
    ServiceTicketResourceTests.class,
    TicketStatusResourceTests.class,
    UserAuthenticationResourceTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
