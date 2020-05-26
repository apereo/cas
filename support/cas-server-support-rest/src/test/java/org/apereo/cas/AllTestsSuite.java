package org.apereo.cas;

import org.apereo.cas.config.CasRestConfigurationTests;
import org.apereo.cas.support.rest.RestResourceUtilsTests;
import org.apereo.cas.support.rest.ServiceTicketResourceTests;
import org.apereo.cas.support.rest.TicketGrantingTicketResourceTests;
import org.apereo.cas.support.rest.TicketStatusResourceTests;
import org.apereo.cas.support.rest.UserAuthenticationResourceTests;

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
    RestResourceUtilsTests.class,
    CasRestConfigurationTests.class,
    TicketGrantingTicketResourceTests.class,
    ServiceTicketResourceTests.class,
    TicketStatusResourceTests.class,
    UserAuthenticationResourceTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
