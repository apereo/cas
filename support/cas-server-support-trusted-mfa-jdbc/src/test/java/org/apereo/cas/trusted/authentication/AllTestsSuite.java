package org.apereo.cas.trusted.authentication;

import org.apereo.cas.trusted.authentication.storage.JpaMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.authentication.storage.MySQLJpaMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.authentication.storage.OracleJpaMultifactorAuthenticationTrustStorageTests;
import org.apereo.cas.trusted.authentication.storage.PostgresJpaMultifactorAuthenticationTrustStorageTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

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
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}

