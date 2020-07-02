package org.apereo.cas;

import org.apereo.cas.services.RegisteredServiceAcceptableUsagePolicyTests;
import org.apereo.cas.services.RegisteredServiceConsentPolicyTests;
import org.apereo.cas.services.RegisteredServicePropertyTests;
import org.apereo.cas.services.ServiceRegistryListenerTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

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
    ServiceRegistryListenerTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
