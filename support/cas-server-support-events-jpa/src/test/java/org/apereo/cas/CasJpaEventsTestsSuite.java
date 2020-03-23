package org.apereo.cas;

import org.apereo.cas.support.events.jpa.JpaCasEventRepositoryTests;
import org.apereo.cas.support.events.jpa.MySQLJpaCasEventRepositoryTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

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
@RunWith(JUnitPlatform.class)
public class CasJpaEventsTestsSuite {
}
