package org.apereo.cas;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.jdbc.QueryAndEncodeDatabaseAuthenticationHandlerTests;
import org.apereo.cas.adaptors.jdbc.QueryDatabaseAuthenticationHandlerTests;
import org.apereo.cas.adaptors.jdbc.SearchModeSearchDatabaseAuthenticationHandlerTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * The {@link AllTestsSuite} is responsible for
 * running all jdbc test cases.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({QueryAndEncodeDatabaseAuthenticationHandlerTests.class, 
        QueryDatabaseAuthenticationHandlerTests.class,
        SearchModeSearchDatabaseAuthenticationHandlerTests.class})
@Slf4j
public class AllTestsSuite {
}
