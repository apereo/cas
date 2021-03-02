package org.apereo.cas;

import org.apereo.cas.acme.AcmeAuthorizationExecutorTests;
import org.apereo.cas.acme.AcmeCertificateManagerTests;
import org.apereo.cas.acme.AcmeChallengeRepositoryTests;
import org.apereo.cas.acme.AcmeWellKnownChallengeControllerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

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
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
