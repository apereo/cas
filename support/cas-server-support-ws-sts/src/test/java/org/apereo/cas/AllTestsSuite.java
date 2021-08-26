package org.apereo.cas;

import org.apereo.cas.ticket.DefaultSecurityTokenTicketFactoryTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @since 6.1.0
 */
@SelectClasses(DefaultSecurityTokenTicketFactoryTests.class)
@Suite
public class AllTestsSuite {
}
