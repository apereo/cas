package org.apereo.cas.consent;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    JpaConsentRepositoryTests.class,
    PostgresJpaConsentRepositoryTests.class,
    OracleJpaConsentRepositoryTests.class,
    MySQLJpaConsentRepositoryTests.class
})
@Suite
public class AllTestsSuite {
}
