package org.apereo.cas;

import org.apereo.cas.authentication.HttpBasedServiceCredentialTests;
import org.apereo.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandlerTests;
import org.apereo.cas.authentication.principal.ResponseTests;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGeneratorTests;
import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImplTests;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactoryTests;
import org.apereo.cas.config.CasServiceRegistryInitializationConfigurationTests;
import org.apereo.cas.config.DomainServicesManagerConfigurationTests;
import org.apereo.cas.services.AnonymousRegisteredServiceUsernameAttributeProviderTests;
import org.apereo.cas.services.ChainingRegisteredServiceSingleSignOnParticipationPolicyTests;
import org.apereo.cas.services.DefaultChainingServicesManagerTests;
import org.apereo.cas.services.DefaultDomainAwareServicesManagerTests;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategyTests;
import org.apereo.cas.services.DefaultRegisteredServiceAuthenticationPolicyTests;
import org.apereo.cas.services.DefaultRegisteredServiceDomainExtractorTests;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicyTests;
import org.apereo.cas.services.DefaultRegisteredServiceProxyTicketExpirationPolicyTests;
import org.apereo.cas.services.DefaultRegisteredServiceServiceTicketExpirationPolicyTests;
import org.apereo.cas.services.DefaultRegisteredServiceUsernameProviderTests;
import org.apereo.cas.services.DefaultServicesManagerByEnvironmentTests;
import org.apereo.cas.services.DefaultServicesManagerCachingTests;
import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocatorTests;
import org.apereo.cas.services.DefaultServicesManagerTests;
import org.apereo.cas.services.FullRegexRegisteredServiceMatchingStrategyTests;
import org.apereo.cas.services.GroovyAuthenticationHandlerResolverTests;
import org.apereo.cas.services.GroovyRegisteredServiceAccessStrategyTests;
import org.apereo.cas.services.GroovyRegisteredServiceMultifactorPolicyTests;
import org.apereo.cas.services.GroovyRegisteredServiceUsernameProviderTests;
import org.apereo.cas.services.InMemoryServiceRegistryTests;
import org.apereo.cas.services.LiteralRegisteredServiceMatchingStrategyTests;
import org.apereo.cas.services.PartialRegexRegisteredServiceMatchingStrategyTests;
import org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProviderTests;
import org.apereo.cas.services.RefuseRegisteredServiceProxyPolicyTests;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicyTests;
import org.apereo.cas.services.RegexRegisteredServiceTests;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtilsTests;
import org.apereo.cas.services.RegisteredServiceAuthenticationHandlerResolverTests;
import org.apereo.cas.services.RegisteredServiceAuthenticationPolicyResolverTests;
import org.apereo.cas.services.RegisteredServicePublicKeyImplTests;
import org.apereo.cas.services.RegisteredServiceTests;
import org.apereo.cas.services.RegisteredServicesEventListenerTests;
import org.apereo.cas.services.RemoteEndpointServiceAccessStrategyTests;
import org.apereo.cas.services.ReturnEncryptedAttributeReleasePolicyTests;
import org.apereo.cas.services.ScriptedRegisteredServiceUsernameProviderTests;
import org.apereo.cas.services.SimpleServiceTests;
import org.apereo.cas.services.TimeBasedRegisteredServiceAccessStrategyTests;
import org.apereo.cas.services.UnauthorizedProxyingExceptionTests;
import org.apereo.cas.services.UnauthorizedServiceExceptionTests;
import org.apereo.cas.services.UnauthorizedSsoServiceExceptionTests;
import org.apereo.cas.services.support.RegisteredServiceChainingAttributeFilterTests;
import org.apereo.cas.services.support.RegisteredServiceMappedRegexAttributeFilterTests;
import org.apereo.cas.services.support.RegisteredServiceMutantRegexAttributeFilterTests;
import org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilterTests;
import org.apereo.cas.services.support.RegisteredServiceReverseMappedRegexAttributeFilterTests;
import org.apereo.cas.services.support.RegisteredServiceScriptedAttributeFilterTests;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializerTests;
import org.apereo.cas.services.util.RegisteredServiceYamlSerializerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllServicesTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@SelectClasses({
    HttpBasedServiceCredentialsAuthenticationHandlerTests.class,
    HttpBasedServiceCredentialTests.class,
    AnonymousRegisteredServiceUsernameAttributeProviderTests.class,
    DefaultRegisteredServiceAccessStrategyTests.class,
    DefaultRegisteredServiceUsernameProviderTests.class,
    DefaultRegisteredServiceMultifactorPolicyTests.class,
    DefaultServicesManagerTests.class,
    DefaultDomainAwareServicesManagerTests.class,
    InMemoryServiceRegistryTests.class,
    PrincipalAttributeRegisteredServiceUsernameProviderTests.class,
    RegexRegisteredServiceTests.class,
    RegexMatchingRegisteredServiceProxyPolicyTests.class,
    RefuseRegisteredServiceProxyPolicyTests.class,
    GroovyRegisteredServiceUsernameProviderTests.class,
    RegisteredServiceAuthenticationHandlerResolverTests.class,
    SimpleServiceTests.class,
    RegisteredServiceMappedRegexAttributeFilterTests.class,
    RegisteredServiceRegexAttributeFilterTests.class,
    RegisteredServicePublicKeyImplTests.class,
    TimeBasedRegisteredServiceAccessStrategyTests.class,
    UnauthorizedProxyingExceptionTests.class,
    UnauthorizedServiceExceptionTests.class,
    UnauthorizedSsoServiceExceptionTests.class,
    ResponseTests.class,
    DefaultServicesManagerCachingTests.class,
    GroovyAuthenticationHandlerResolverTests.class,
    RegisteredServicesEventListenerTests.class,
    DefaultRegisteredServiceDomainExtractorTests.class,
    ChainingRegisteredServiceSingleSignOnParticipationPolicyTests.class,
    DefaultRegisteredServiceProxyTicketExpirationPolicyTests.class,
    DefaultRegisteredServiceServiceTicketExpirationPolicyTests.class,
    DefaultServicesManagerByEnvironmentTests.class,
    ScriptedRegisteredServiceUsernameProviderTests.class,
    RemoteEndpointServiceAccessStrategyTests.class,
    ShibbolethCompatiblePersistentIdGeneratorTests.class,
    SimpleWebApplicationServiceImplTests.class,
    WebApplicationServiceFactoryTests.class,
    CasServiceRegistryInitializationConfigurationTests.class,
    DefaultRegisteredServiceAuthenticationPolicyTests.class,
    UnauthorizedProxyingExceptionTests.class,
    RegisteredServiceTests.class,
    LiteralRegisteredServiceMatchingStrategyTests.class,
    PartialRegexRegisteredServiceMatchingStrategyTests.class,
    FullRegexRegisteredServiceMatchingStrategyTests.class,
    RegisteredServiceReverseMappedRegexAttributeFilterTests.class,
    ReturnEncryptedAttributeReleasePolicyTests.class,
    UnauthorizedServiceExceptionTests.class,
    DefaultServicesManagerRegisteredServiceLocatorTests.class,
    RegisteredServiceChainingAttributeFilterTests.class,
    RegisteredServiceAccessStrategyUtilsTests.class,
    RegisteredServiceYamlSerializerTests.class,
    RegisteredServiceAuthenticationPolicyResolverTests.class,
    UnauthorizedSsoServiceExceptionTests.class,
    GroovyRegisteredServiceMultifactorPolicyTests.class,
    RegisteredServiceMutantRegexAttributeFilterTests.class,
    RegisteredServiceScriptedAttributeFilterTests.class,
    GroovyRegisteredServiceAccessStrategyTests.class,
    DefaultChainingServicesManagerTests.class,
    DomainServicesManagerConfigurationTests.class,
    RegisteredServiceJsonSerializerTests.class
})
@Suite
public class AllServicesTestsSuite {
}
