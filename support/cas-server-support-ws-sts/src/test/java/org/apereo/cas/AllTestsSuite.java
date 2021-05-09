package org.apereo.cas;

import org.apereo.cas.ticket.DefaultSecurityTokenTicketFactoryTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @since 6.1.0
 */
@SelectClasses(DefaultSecurityTokenTicketFactoryTests.class)
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
