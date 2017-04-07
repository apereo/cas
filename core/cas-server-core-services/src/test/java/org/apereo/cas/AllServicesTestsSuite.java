package org.apereo.cas;

import org.apereo.cas.authentication.DefaultMultifactorTriggerSelectionStrategyTests;
import org.apereo.cas.authentication.HttpBasedServiceCredentialTests;
import org.apereo.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandlerTests;
import org.apereo.cas.authentication.principal.ResponseTests;
import org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGeneratorTests;
import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImplTests;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactoryTests;
import org.apereo.cas.authentication.support.DefaultCasAttributeEncoderTests;
import org.apereo.cas.services.AnonymousRegisteredServiceUsernameAttributeProviderTests;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategyTests;
import org.apereo.cas.services.DefaultRegisteredServiceUsernameProviderTests;
import org.apereo.cas.services.DefaultServicesManagerTests;
import org.apereo.cas.services.InMemoryServiceRegistryTests;
import org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProviderTests;
import org.apereo.cas.services.RegexRegisteredServiceTests;
import org.apereo.cas.services.RegisteredServiceAuthenticationHandlerResolverTests;
import org.apereo.cas.services.SimpleServiceTests;
import org.apereo.cas.services.TimeBasedRegisteredServiceAccessStrategyTests;
import org.apereo.cas.services.UnauthorizedProxyingExceptionTests;
import org.apereo.cas.services.UnauthorizedServiceExceptionTests;
import org.apereo.cas.services.UnauthorizedSsoServiceExceptionTests;
import org.apereo.cas.util.services.RegisteredServiceJsonSerializerTests;
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
        DefaultServicesManagerTests.class,
        InMemoryServiceRegistryTests.class,
        PrincipalAttributeRegisteredServiceUsernameProviderTests.class,
        RegexRegisteredServiceTests.class,
        RegisteredServiceAuthenticationHandlerResolverTests.class,
        SimpleServiceTests.class,
        TimeBasedRegisteredServiceAccessStrategyTests.class,
        UnauthorizedProxyingExceptionTests.class,
        UnauthorizedServiceExceptionTests.class,
        UnauthorizedSsoServiceExceptionTests.class,
        ResponseTests.class, 
        ShibbolethCompatiblePersistentIdGeneratorTests.class,
        SimpleWebApplicationServiceImplTests.class, 
        WebApplicationServiceFactoryTests.class,
        RegisteredServiceJsonSerializerTests.class,
        DefaultCasAttributeEncoderTests.class})
public class AllServicesTestsSuite {
}
