package org.apereo.cas;

import org.apereo.cas.authentication.principal.DelegatedClientAuthenticationCredentialResolverTests;
import org.apereo.cas.authentication.principal.GroovyDelegatedClientAuthenticationCredentialResolverTests;
import org.apereo.cas.authentication.principal.ldap.LdapDelegatedClientAuthenticationCredentialResolverTests;
import org.apereo.cas.support.pac4j.RefreshableDelegatedClientsTests;
import org.apereo.cas.support.pac4j.clients.DefaultDelegatedClientFactoryTests;
import org.apereo.cas.support.pac4j.clients.DelegatedClientsEndpointTests;
import org.apereo.cas.support.pac4j.clients.GroovyDelegatedClientAuthenticationRequestCustomizerTests;
import org.apereo.cas.support.pac4j.clients.RestfulDelegatedClientFactoryTests;
import org.apereo.cas.web.DefaultDelegatedAuthenticationNavigationControllerTests;
import org.apereo.cas.web.DelegatedClientIdentityProviderRedirectionStrategyTests;
import org.apereo.cas.web.flow.DefaultDelegatedClientAuthenticationWebflowManagerTests;
import org.apereo.cas.web.flow.DefaultDelegatedClientIdentityProviderConfigurationProducerTests;
import org.apereo.cas.web.flow.DelegatedAuthenticationErrorViewResolverTests;
import org.apereo.cas.web.flow.DelegatedAuthenticationSAMLConfigurationTests;
import org.apereo.cas.web.flow.DelegatedAuthenticationWebflowConfigurerTests;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationActionTests;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationGroovyPostProcessorTests;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationPostProcessorTests;
import org.apereo.cas.web.flow.DelegatedClientIdentityProviderConfigurationProducerTests;
import org.apereo.cas.web.flow.actions.DelegatedAuthenticationClientFinishLogoutActionTests;
import org.apereo.cas.web.flow.actions.DelegatedAuthenticationClientLogoutActionTests;
import org.apereo.cas.web.flow.actions.DelegatedAuthenticationClientRetryActionTests;
import org.apereo.cas.web.flow.actions.DelegatedAuthenticationGenerateClientsActionTests;
import org.apereo.cas.web.flow.actions.DelegatedClientAuthenticationCredentialSelectionActionTests;
import org.apereo.cas.web.flow.actions.DelegatedClientAuthenticationCredentialSelectionFinalizeActionTests;
import org.apereo.cas.web.flow.actions.DelegatedClientAuthenticationDynamicDiscoveryExecutionActionTests;
import org.apereo.cas.web.flow.actions.DelegatedClientAuthenticationFailureActionTests;
import org.apereo.cas.web.flow.actions.DelegatedClientAuthenticationRedirectActionTests;
import org.apereo.cas.web.flow.actions.DelegatedClientAuthenticationStoreWebflowStateActionTests;
import org.apereo.cas.web.flow.authz.DefaultDelegatedClientIdentityProviderAuthorizerTests;
import org.apereo.cas.web.saml2.DelegatedSaml2ClientMetadataControllerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link DelegatedAuthenticationTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    DelegatedClientIdentityProviderRedirectionStrategyTests.class,
    DelegatedClientAuthenticationRedirectActionTests.class,
    DelegatedAuthenticationGenerateClientsActionTests.class,
    RefreshableDelegatedClientsTests.class,
    DelegatedClientsEndpointTests.class,
    DefaultDelegatedClientFactoryTests.class,
    GroovyDelegatedClientAuthenticationCredentialResolverTests.class,
    DelegatedClientAuthenticationCredentialResolverTests.class,
    RestfulDelegatedClientFactoryTests.class,
    LdapDelegatedClientAuthenticationCredentialResolverTests.class,
    DelegatedClientAuthenticationStoreWebflowStateActionTests.class,
    DelegatedClientAuthenticationFailureActionTests.class,
    GroovyDelegatedClientAuthenticationRequestCustomizerTests.class,
    DelegatedClientIdentityProviderConfigurationPostProcessorTests.class,
    DelegatedAuthenticationSAMLConfigurationTests.class,
    DelegatedClientIdentityProviderConfigurationGroovyPostProcessorTests.class,
    DelegatedClientAuthenticationDynamicDiscoveryExecutionActionTests.class,
    DelegatedClientAuthenticationActionTests.class,
    DelegatedClientIdentityProviderConfigurationProducerTests.class,
    DelegatedAuthenticationErrorViewResolverTests.class,
    DefaultDelegatedAuthenticationNavigationControllerTests.class,
    DelegatedAuthenticationClientLogoutActionTests.class,
    DelegatedAuthenticationClientFinishLogoutActionTests.class,
    DefaultDelegatedClientAuthenticationWebflowManagerTests.class,
    DefaultDelegatedClientIdentityProviderConfigurationProducerTests.class,
    DelegatedAuthenticationClientRetryActionTests.class,
    DefaultDelegatedClientIdentityProviderAuthorizerTests.class,
    DelegatedAuthenticationWebflowConfigurerTests.class,
    DelegatedClientAuthenticationCredentialSelectionActionTests.class,
    DelegatedClientAuthenticationCredentialSelectionFinalizeActionTests.class,
    DelegatedSaml2ClientMetadataControllerTests.class
})
@Suite
public class DelegatedAuthenticationTestsSuite {
}
