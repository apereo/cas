package org.apereo.cas.trusted.authentication;

import org.apereo.cas.trusted.authentication.storage.JpaMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.authentication.storage.MySQLJpaMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.authentication.storage.OracleJpaMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.authentication.storage.PostgresJpaMultifactorAuthenticationTrustStorageTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * The {@link org.apereo.cas.AllTestsSuite} is responsible for
 * running all cas test cases.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses({
    JpaMultifactorAuthenticationTrustStorageTests.class,
    OracleJpaMultifactorAuthenticationTrustStorageTests.class,
    PostgresJpaMultifactorAuthenticationTrustStorageTests.class,
    MySQLJpaMultifactorAuthenticationTrustStorageTests.class
})
@Suite
public class AllTestsSuite {
}

