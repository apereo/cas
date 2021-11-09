package org.apereo.cas;

import org.apereo.cas.consent.CouchDbConsentDecisionTests;
import org.apereo.cas.consent.CouchDbConsentRepositoryTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    CouchDbConsentDecisionTests.class,
    CouchDbConsentRepositoryTests.class
})
@Suite
public class AllTestsSuite {
}
