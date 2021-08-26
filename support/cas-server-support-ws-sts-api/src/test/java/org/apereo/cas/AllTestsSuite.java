package org.apereo.cas;

import org.apereo.cas.support.claims.CustomNamespaceWSFederationClaimsClaimsHandlerTests;
import org.apereo.cas.support.claims.WrappingSecurityTokenServiceClaimsHandlerTests;
import org.apereo.cas.support.realm.RealmPasswordVerificationCallbackHandlerTests;
import org.apereo.cas.support.realm.UriRealmParserTests;
import org.apereo.cas.support.saml.SamlAssertionRealmCodecTests;
import org.apereo.cas.support.validation.CipheredCredentialsValidatorTests;
import org.apereo.cas.support.validation.SecurityTokenServiceCredentialCipherExecutorTests;
import org.apereo.cas.support.x509.X509TokenDelegationHandlerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @since 6.1.0
 */
@SelectClasses({
    SamlAssertionRealmCodecTests.class,
    X509TokenDelegationHandlerTests.class,
    SecurityTokenServiceCredentialCipherExecutorTests.class,
    UriRealmParserTests.class,
    RealmPasswordVerificationCallbackHandlerTests.class,
    WrappingSecurityTokenServiceClaimsHandlerTests.class,
    CustomNamespaceWSFederationClaimsClaimsHandlerTests.class,
    CipheredCredentialsValidatorTests.class
})
@Suite
public class AllTestsSuite {
}
