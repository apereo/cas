package org.apereo.cas;

import org.apereo.cas.acme.AcmeAuthorizationExecutorTests;
import org.apereo.cas.acme.AcmeCertificateManagerTests;
import org.apereo.cas.acme.AcmeChallengeRepositoryTests;
import org.apereo.cas.acme.AcmeWellKnownChallengeControllerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses({
    AcmeCertificateManagerTests.class,
    AcmeChallengeRepositoryTests.class,
    AcmeAuthorizationExecutorTests.class,
    AcmeWellKnownChallengeControllerTests.class
})
@Suite
public class AllTestsSuite {
}
