package org.apereo.cas;

import org.apereo.cas.adaptors.x509.authentication.handler.support.CRLDistributionPointRevocationCheckerTests;
import org.apereo.cas.adaptors.x509.authentication.handler.support.ResourceCRLRevocationCheckerTests;
import org.apereo.cas.adaptors.x509.authentication.handler.support.ThresholdExpiredCRLRevocationPolicyTests;
import org.apereo.cas.adaptors.x509.authentication.handler.support.X509CredentialsAuthenticationHandlerTests;
import org.apereo.cas.adaptors.x509.authentication.principal.X509SerialNumberAndIssuerDNPrincipalResolverTests;
import org.apereo.cas.adaptors.x509.authentication.principal.X509SerialNumberPrincipalResolverTests;
import org.apereo.cas.adaptors.x509.authentication.principal.X509SubjectAlternativeNameUPNPrincipalResolverTests;
import org.apereo.cas.adaptors.x509.authentication.principal.X509SubjectDNPrincipalResolverTests;
import org.apereo.cas.adaptors.x509.authentication.principal.X509SubjectPrincipalResolverTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * The {@link AllLdapTestsSuite} is responsible for
 * running all cas test cases.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        X509SerialNumberAndIssuerDNPrincipalResolverTests.class,
        X509SerialNumberPrincipalResolverTests.class,
        X509SubjectDNPrincipalResolverTests.class,
        X509SubjectAlternativeNameUPNPrincipalResolverTests.class,
        X509SubjectPrincipalResolverTests.class,
        ResourceCRLRevocationCheckerTests.class,
        ThresholdExpiredCRLRevocationPolicyTests.class,
        X509CredentialsAuthenticationHandlerTests.class,
        CRLDistributionPointRevocationCheckerTests.class})
public class AllTestsSuite {
}
