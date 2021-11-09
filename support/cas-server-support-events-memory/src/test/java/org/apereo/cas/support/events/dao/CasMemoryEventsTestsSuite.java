package org.apereo.cas.support.events.dao;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

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
@Suite
public class CasMemoryEventsTestsSuite {
}
