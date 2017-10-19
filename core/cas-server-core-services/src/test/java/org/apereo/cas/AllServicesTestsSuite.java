package org.apereo.cas;

import org.apereo.cas.authentication.DefaultMultifactorTriggerSelectionStrategyTests;
import org.apereo.cas.authentication.HttpBasedServiceCredentialTests;
import org.apereo.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandlerTests;
import org.apereo.cas.authentication.principal.ResponseTests;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGeneratorTests;
import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImplTests;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactoryTests;
import org.apereo.cas.services.AnonymousRegisteredServiceUsernameAttributeProviderTests;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategyTests;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicyTests;
import org.apereo.cas.services.DefaultRegisteredServiceUsernameProviderTests;
import org.apereo.cas.services.DefaultServicesManagerTests;
import org.apereo.cas.services.DomainServicesManagerTests;
import org.apereo.cas.services.GroovyRegisteredServiceUsernameProviderTests;
import org.apereo.cas.services.InMemoryServiceRegistryTests;
import org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProviderTests;
import org.apereo.cas.services.RefuseRegisteredServiceProxyPolicyTests;
import org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicyTests;
import org.apereo.cas.services.RegexRegisteredServiceTests;
import org.apereo.cas.services.RegisteredServiceAuthenticationHandlerResolverTests;
import org.apereo.cas.services.RegisteredServicePublicKeyImplTests;
import org.apereo.cas.services.RemoteEndpointServiceAccessStrategyTests;
import org.apereo.cas.services.ScriptedRegisteredServiceUsernameProviderTests;
import org.apereo.cas.services.SimpleServiceTests;
import org.apereo.cas.services.TimeBasedRegisteredServiceAccessStrategyTests;
import org.apereo.cas.services.UnauthorizedProxyingExceptionTests;
import org.apereo.cas.services.UnauthorizedServiceExceptionTests;
import org.apereo.cas.services.UnauthorizedSsoServiceExceptionTests;
import org.apereo.cas.services.support.RegisteredServiceMappedRegexAttributeFilterTests;
import org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilterTests;
import org.apereo.cas.util.services.DefaultRegisteredServiceJsonSerializerTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllServicesTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({HttpBasedServiceCredentialsAuthenticationHandlerTests.class, 
        DefaultMultifactorTriggerSelectionStrategyTests.class,
        HttpBasedServiceCredentialTests.class,
        AnonymousRegisteredServiceUsernameAttributeProviderTests.class,
        DefaultRegisteredServiceAccessStrategyTests.class,
        DefaultRegisteredServiceUsernameProviderTests.class,
        DefaultRegisteredServiceMultifactorPolicyTests.class,
        DefaultServicesManagerTests.class,
        DomainServicesManagerTests.class,
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
        ScriptedRegisteredServiceUsernameProviderTests.class,
        RemoteEndpointServiceAccessStrategyTests.class,
        ShibbolethCompatiblePersistentIdGeneratorTests.class,
        SimpleWebApplicationServiceImplTests.class, 
        WebApplicationServiceFactoryTests.class,
        UnauthorizedProxyingExceptionTests.class,
        UnauthorizedServiceExceptionTests.class,
        UnauthorizedSsoServiceExceptionTests.class,
        DefaultRegisteredServiceJsonSerializerTests.class})
public class AllServicesTestsSuite {
}
