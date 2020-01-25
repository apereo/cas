package org.apereo.cas.support.events.dao;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link CasMemoryEventsTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SelectClasses({
    GroovyCasEventRepositoryFilterTests.class,
    InMemoryCasEventRepositoryTests.class
})
@RunWith(JUnitPlatform.class)
public class CasMemoryEventsTestsSuite {
}
