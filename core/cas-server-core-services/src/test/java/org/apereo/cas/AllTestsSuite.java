package org.apereo.cas;

import org.apereo.cas.authentication.DefaultMultifactorTriggerSelectionStrategyTest;
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
import org.apereo.cas.services.DefaultServicesManagerImplTests;
import org.apereo.cas.services.InMemoryServiceRegistryDaoImplTests;
import org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProviderTests;
import org.apereo.cas.services.RegexRegisteredServiceTests;
import org.apereo.cas.services.RegisteredServiceAuthenticationHandlerResolverTests;
import org.apereo.cas.services.SimpleServiceTests;
import org.apereo.cas.services.TimeBasedRegisteredServiceAccessStrategyTests;
import org.apereo.cas.services.UnauthorizedProxyingExceptionTests;
import org.apereo.cas.services.UnauthorizedServiceExceptionTests;
import org.apereo.cas.services.UnauthorizedSsoServiceExceptionTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({HttpBasedServiceCredentialsAuthenticationHandlerTests.class, 
        DefaultMultifactorTriggerSelectionStrategyTest.class,
        HttpBasedServiceCredentialTests.class,
        AnonymousRegisteredServiceUsernameAttributeProviderTests.class,
        DefaultRegisteredServiceAccessStrategyTests.class,
        DefaultRegisteredServiceUsernameProviderTests.class,
        DefaultServicesManagerImplTests.class,
        InMemoryServiceRegistryDaoImplTests.class,
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
        DefaultCasAttributeEncoderTests.class})
public class AllTestsSuite {
}
