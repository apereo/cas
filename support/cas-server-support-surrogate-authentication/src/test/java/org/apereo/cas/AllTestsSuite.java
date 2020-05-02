
package org.apereo.cas;

import org.apereo.cas.authentication.SurrogateAuthenticationExpirationPolicyBuilderTests;
import org.apereo.cas.authentication.SurrogateAuthenticationMetaDataPopulatorTests;
import org.apereo.cas.authentication.SurrogateAuthenticationPostProcessorTests;
import org.apereo.cas.authentication.SurrogatePrincipalBuilderTests;
import org.apereo.cas.authentication.SurrogatePrincipalElectionStrategyTests;
import org.apereo.cas.authentication.SurrogatePrincipalResolverTests;
import org.apereo.cas.authentication.audit.SurrogateAuditPrincipalIdProviderTests;
import org.apereo.cas.authentication.audit.SurrogateEligibilityVerificationAuditResourceResolverTests;
import org.apereo.cas.authentication.event.SurrogateAuthenticationEventListenerTests;
import org.apereo.cas.authentication.rest.SurrogateAuthenticationRestHttpRequestCredentialFactoryTests;
import org.apereo.cas.authentication.surrogate.JsonResourceSurrogateAuthenticationServiceTests;
import org.apereo.cas.authentication.surrogate.SimpleSurrogateAuthenticationServiceTests;
import org.apereo.cas.ticket.expiration.SurrogateSessionExpirationPolicyJsonSerializerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0-RC3
 */
@SelectClasses({
    SurrogateAuthenticationPostProcessorTests.class,
    SurrogateAuthenticationMetaDataPopulatorTests.class,
    SurrogatePrincipalResolverTests.class,
    SurrogatePrincipalBuilderTests.class,
    SurrogateAuthenticationExpirationPolicyBuilderTests.class,
    SurrogateAuthenticationEventListenerTests.class,
    SurrogateEligibilityVerificationAuditResourceResolverTests.class,
    SurrogateAuthenticationRestHttpRequestCredentialFactoryTests.class,
    SurrogateAuditPrincipalIdProviderTests.class,
    SimpleSurrogateAuthenticationServiceTests.class,
    JsonResourceSurrogateAuthenticationServiceTests.class,
    SurrogatePrincipalElectionStrategyTests.class,
    SurrogateSessionExpirationPolicyJsonSerializerTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
