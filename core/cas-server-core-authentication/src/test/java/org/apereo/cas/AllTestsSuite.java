package org.apereo.cas;

import org.apereo.cas.authentication.AcceptUsersAuthenticationHandlerTests;
import org.apereo.cas.authentication.BasicCredentialMetaDataTests;
import org.apereo.cas.authentication.CacheCredentialsMetaDataPopulatorTests;
import org.apereo.cas.authentication.CoreAuthenticationUtilsTests;
import org.apereo.cas.authentication.DefaultAuthenticationTests;
import org.apereo.cas.authentication.DefaultPasswordEncoderTests;
import org.apereo.cas.authentication.FileTrustStoreSslSocketFactoryTests;
import org.apereo.cas.authentication.OneTimePasswordCredentialTests;
import org.apereo.cas.authentication.RememberMePasswordCredentialTests;
import org.apereo.cas.authentication.UsernamePasswordCredentialTests;
import org.apereo.cas.authentication.exceptions.UnresolvedPrincipalExceptionTests;
import org.apereo.cas.authentication.handler.ConvertCasePrincipalNameTransformerTests;
import org.apereo.cas.authentication.handler.support.JaasAuthenticationHandlerSystemConfigurationTests;
import org.apereo.cas.authentication.handler.support.JaasAuthenticationHandlerTests;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordHandlerTests;
import org.apereo.cas.authentication.metadata.CacheCredentialsCipherExecutorTests;
import org.apereo.cas.authentication.policy.AllAuthenticationHandlersSucceededAuthenticationPolicyTests;
import org.apereo.cas.authentication.policy.NotPreventedAuthenticationPolicyTests;
import org.apereo.cas.authentication.policy.RestfulAuthenticationPolicyTests;
import org.apereo.cas.authentication.policy.UniquePrincipalAuthenticationPolicyTests;
import org.apereo.cas.authentication.principal.ChainingPrincipalElectionStrategyTests;
import org.apereo.cas.authentication.principal.ChainingPrincipalResolverTests;
import org.apereo.cas.authentication.principal.NullPrincipalTests;
import org.apereo.cas.authentication.principal.RememberMeAuthenticationMetaDataPopulatorTests;
import org.apereo.cas.authentication.principal.SimplePrincipalFactoryTests;
import org.apereo.cas.authentication.principal.SimplePrincipalTests;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtilsTests;

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
    SimplePrincipalTests.class,
    ChainingPrincipalResolverTests.class,
    NullPrincipalTests.class,
    PasswordEncoderUtilsTests.class,
    SimplePrincipalFactoryTests.class,
    RememberMeAuthenticationMetaDataPopulatorTests.class,
    ConvertCasePrincipalNameTransformerTests.class,
    JaasAuthenticationHandlerSystemConfigurationTests.class,
    JaasAuthenticationHandlerTests.class,
    SimpleTestUsernamePasswordHandlerTests.class,
    CoreAuthenticationUtilsTests.class,
    UsernamePasswordCredentialTests.class,
    DefaultPasswordEncoderTests.class,
    DefaultAuthenticationTests.class,
    CacheCredentialsCipherExecutorTests.class,
    NotPreventedAuthenticationPolicyTests.class,
    AllAuthenticationHandlersSucceededAuthenticationPolicyTests.class,
    BasicCredentialMetaDataTests.class,
    ChainingPrincipalElectionStrategyTests.class,
    UsernamePasswordCredentialTests.class,
    UnresolvedPrincipalExceptionTests.class,
    RememberMePasswordCredentialTests.class,
    AcceptUsersAuthenticationHandlerTests.class,
    CacheCredentialsMetaDataPopulatorTests.class,
    OneTimePasswordCredentialTests.class,
    UniquePrincipalAuthenticationPolicyTests.class,
    RestfulAuthenticationPolicyTests.class,
    FileTrustStoreSslSocketFactoryTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
