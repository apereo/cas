package org.apereo.cas.consent;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link AllLdapConsentTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({LdapContinuousIntegrationConsentRepositoryTests.class,
    LdapEmbeddedConsentRepositoryTests.class})
public class AllLdapConsentTestsSuite {

}
