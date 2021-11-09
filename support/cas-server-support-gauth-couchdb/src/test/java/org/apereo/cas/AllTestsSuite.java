
package org.apereo.cas;

import org.apereo.cas.gauth.credential.CouchDbGoogleAuthenticatorTokenCredentialRepositoryTests;
import org.apereo.cas.gauth.token.GoogleAuthenticatorCouchDbTokenRepositoryTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    GoogleAuthenticatorCouchDbTokenRepositoryTests.class,
    CouchDbGoogleAuthenticatorTokenCredentialRepositoryTests.class
})
@Suite
public class AllTestsSuite {
}
