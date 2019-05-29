package org.apereo.cas.consent;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllLdapConsentTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    LdapContinuousIntegrationConsentRepositoryTests.class,
    LdapEmbeddedConsentRepositoryTests.class
})
@RunWith(JUnitPlatform.class)
public class AllLdapConsentTestsSuite {
}
