package org.apereo.cas;

import org.apereo.cas.authentication.principal.RegisteredServicePrincipalAttributesRepositoryTests;
import org.apereo.cas.services.RegisteredServiceAcceptableUsagePolicyTests;
import org.apereo.cas.services.RegisteredServiceAccessStrategyTests;
import org.apereo.cas.services.RegisteredServiceCipherExecutorTests;
import org.apereo.cas.services.RegisteredServiceConsentPolicyTests;
import org.apereo.cas.services.RegisteredServiceDelegatedAuthenticationPolicyTests;
import org.apereo.cas.services.RegisteredServicePropertyTests;
import org.apereo.cas.services.ServiceRegistryListenerTests;
import org.apereo.cas.services.ServiceRegistryTests;
import org.apereo.cas.services.ServicesManagerTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * The {@link AllTestsSuite} is responsible for
 * running all cas test cases.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses({
    RegisteredServiceAcceptableUsagePolicyTests.class,
    RegisteredServiceConsentPolicyTests.class,
    RegisteredServicePropertyTests.class,
    ServiceRegistryTests.class,
    RegisteredServicePrincipalAttributesRepositoryTests.class,
    RegisteredServiceDelegatedAuthenticationPolicyTests.class,
    ServicesManagerTests.class,
    RegisteredServiceCipherExecutorTests.class,
    RegisteredServiceAccessStrategyTests.class,
    ServiceRegistryListenerTests.class
})
@Suite
public class AllTestsSuite {
}
