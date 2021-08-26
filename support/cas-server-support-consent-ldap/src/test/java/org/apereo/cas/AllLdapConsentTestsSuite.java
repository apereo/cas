package org.apereo.cas;

import org.apereo.cas.consent.LdapConsentRepositoryTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllLdapConsentTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses(LdapConsentRepositoryTests.class)
@Suite
public class AllLdapConsentTestsSuite {
}
