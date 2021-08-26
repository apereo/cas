package org.apereo.cas;

import org.apereo.cas.support.events.jpa.JpaCasEventRepositoryTests;
import org.apereo.cas.support.events.jpa.MySQLJpaCasEventRepositoryTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link CasJpaEventsTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    JpaCasEventRepositoryTests.class,
    MySQLJpaCasEventRepositoryTests.class
})
@Suite
public class CasJpaEventsTestsSuite {
}
