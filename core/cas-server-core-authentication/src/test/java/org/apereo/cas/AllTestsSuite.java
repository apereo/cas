package org.apereo.cas;

import org.apereo.cas.authentication.AcceptUsersAuthenticationHandlerTests;
import org.apereo.cas.authentication.BasicCredentialMetaDataTests;
import org.apereo.cas.authentication.CacheCredentialsMetaDataPopulatorTests;
import org.apereo.cas.authentication.ClientInfoAuthenticationMetaDataPopulatorTests;
import org.apereo.cas.authentication.CoreAuthenticationUtilsTests;
import org.apereo.cas.authentication.DefaultAuthenticationEventExecutionPlanTests;
import org.apereo.cas.authentication.DefaultAuthenticationManagerTests;
import org.apereo.cas.authentication.DefaultAuthenticationTests;
import org.apereo.cas.authentication.DefaultPasswordEncoderTests;
import org.apereo.cas.authentication.FileTrustStoreSslSocketFactoryTests;
import org.apereo.cas.authentication.OneTimePasswordCredentialTests;
import org.apereo.cas.authentication.RememberMePasswordCredentialTests;
import org.apereo.cas.authentication.UsernamePasswordCredentialTests;
import org.apereo.cas.authentication.attribute.DefaultAttributeDefinitionTests;
import org.apereo.cas.authentication.exceptions.UnresolvedPrincipalExceptionTests;
import org.apereo.cas.authentication.handler.BlockingPrincipalNameTransformerTests;
import org.apereo.cas.authentication.handler.ConvertCasePrincipalNameTransformerTests;
import org.apereo.cas.authentication.handler.support.JaasAuthenticationHandlerSystemConfigurationTests;
import org.apereo.cas.authentication.handler.support.JaasAuthenticationHandlerTests;
import org.apereo.cas.authentication.handler.support.JaasAuthenticationHandlersConfigurationTests;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordHandlerTests;
import org.apereo.cas.authentication.handler.support.jaas.AccountsPreDefinedLoginModuleTests;
import org.apereo.cas.authentication.metadata.CacheCredentialsCipherExecutorTests;
import org.apereo.cas.authentication.policy.AllAuthenticationHandlersSucceededAuthenticationPolicyTests;
import org.apereo.cas.authentication.policy.AtLeastOneCredentialValidatedAuthenticationPolicyTests;
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
import org.apereo.cas.config.CasCoreAuthenticationSupportConfigurationTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

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
    DefaultAuthenticationEventExecutionPlanTests.class,
    JaasAuthenticationHandlersConfigurationTests.class,
    PasswordEncoderUtilsTests.class,
    AccountsPreDefinedLoginModuleTests.class,
    AtLeastOneCredentialValidatedAuthenticationPolicyTests.class,
    SimplePrincipalFactoryTests.class,
    RememberMeAuthenticationMetaDataPopulatorTests.class,
    ConvertCasePrincipalNameTransformerTests.class,
    JaasAuthenticationHandlerSystemConfigurationTests.class,
    JaasAuthenticationHandlerTests.class,
    SimpleTestUsernamePasswordHandlerTests.class,
    CoreAuthenticationUtilsTests.class,
    DefaultAttributeDefinitionTests.class,
    UsernamePasswordCredentialTests.class,
    DefaultPasswordEncoderTests.class,
    DefaultAuthenticationTests.class,
    CacheCredentialsCipherExecutorTests.class,
    NotPreventedAuthenticationPolicyTests.class,
    AllAuthenticationHandlersSucceededAuthenticationPolicyTests.class,
    BasicCredentialMetaDataTests.class,
    DefaultAuthenticationManagerTests.class,
    ClientInfoAuthenticationMetaDataPopulatorTests.class,
    CasCoreAuthenticationSupportConfigurationTests.class,
    ChainingPrincipalElectionStrategyTests.class,
    UsernamePasswordCredentialTests.class,
    UnresolvedPrincipalExceptionTests.class,
    RememberMePasswordCredentialTests.class,
    AcceptUsersAuthenticationHandlerTests.class,
    CacheCredentialsMetaDataPopulatorTests.class,
    OneTimePasswordCredentialTests.class,
    BlockingPrincipalNameTransformerTests.class,
    UniquePrincipalAuthenticationPolicyTests.class,
    RestfulAuthenticationPolicyTests.class,
    FileTrustStoreSslSocketFactoryTests.class
})
@Suite
public class AllTestsSuite {
}
