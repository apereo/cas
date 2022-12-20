package org.apereo.cas;

import org.apereo.cas.mail.SendGridEmailSenderTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * The {@link AllTestsSuite} is responsible for
 * running all cas test cases.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses(SendGridEmailSenderTests.class)
@Suite
public class AllTestsSuite {
}

