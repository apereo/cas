package org.apereo.cas;

import org.apereo.cas.adaptors.jdbc.BindModeSearchDatabaseAuthenticationHandlerTests;
import org.apereo.cas.adaptors.jdbc.NamedQueryDatabaseAuthenticationHandlerTests;
import org.apereo.cas.adaptors.jdbc.QueryAndEncodeDatabaseAuthenticationHandlerTests;
import org.apereo.cas.adaptors.jdbc.QueryDatabaseAuthenticationHandlerMariaDbTests;
import org.apereo.cas.adaptors.jdbc.QueryDatabaseAuthenticationHandlerPostgresTests;
import org.apereo.cas.adaptors.jdbc.QueryDatabaseAuthenticationHandlerTests;
import org.apereo.cas.adaptors.jdbc.SearchModeSearchDatabaseAuthenticationHandlerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * The {@link AllTestsSuite} is responsible for
 * running all jdbc test cases.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses({
    QueryAndEncodeDatabaseAuthenticationHandlerTests.class,
    QueryDatabaseAuthenticationHandlerTests.class,
    QueryDatabaseAuthenticationHandlerMariaDbTests.class,
    QueryDatabaseAuthenticationHandlerPostgresTests.class,
    NamedQueryDatabaseAuthenticationHandlerTests.class,
    BindModeSearchDatabaseAuthenticationHandlerTests.class,
    SearchModeSearchDatabaseAuthenticationHandlerTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
