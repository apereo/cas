package org.apereo.cas;

import org.apereo.cas.adaptors.trusted.authentication.handler.support.PrincipalBearingCredentialsAuthenticationHandlerTests;
import org.apereo.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredentialsTests;
import org.apereo.cas.adaptors.trusted.authentication.principal.PrincipalBearingCredentialsToPrincipalResolverTests;
import org.apereo.cas.adaptors.trusted.web.flow.PrincipalFromRemoteRequestHeaderNonInteractiveCredentialsActionTests;
import org.apereo.cas.adaptors.trusted.web.flow.PrincipalFromRequestHeaderNonInteractiveCredentialsActionTests;
import org.apereo.cas.adaptors.trusted.web.flow.PrincipalFromRequestRemoteUserNonInteractiveCredentialsActionTests;
import org.apereo.cas.adaptors.trusted.web.flow.PrincipalFromRequestUserPrincipalNonInteractiveCredentialsActionTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * This is {@link AllTrustedTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@SelectClasses({
    PrincipalBearingCredentialsAuthenticationHandlerTests.class,
    PrincipalFromRequestHeaderNonInteractiveCredentialsActionTests.class,
    PrincipalBearingCredentialsTests.class,
    PrincipalFromRemoteRequestHeaderNonInteractiveCredentialsActionTests.class,
    PrincipalBearingCredentialsToPrincipalResolverTests.class,
    PrincipalFromRequestRemoteUserNonInteractiveCredentialsActionTests.class,
    PrincipalFromRequestUserPrincipalNonInteractiveCredentialsActionTests.class
})
public class AllTrustedTestsSuite {
}
