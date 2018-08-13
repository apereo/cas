package org.apereo.cas.consent;

import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllLdapConsentTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(Enclosed.class)
@Suite.SuiteClasses({LdapContinuousIntegrationConsentRepositoryTests.class,
    LdapEmbeddedConsentRepositoryTests.class})
public class AllLdapConsentTestsSuite {

}
