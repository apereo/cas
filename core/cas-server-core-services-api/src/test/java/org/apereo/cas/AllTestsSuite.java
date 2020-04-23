package org.apereo.cas;

import org.apereo.cas.services.DefaultRegisteredServicePropertyTests;
import org.apereo.cas.services.support.RegisteredServiceReverseMappedRegexAttributeFilterTests;
import org.apereo.cas.services.util.CasAddonsRegisteredServicesJsonSerializerTests;
import org.apereo.cas.services.util.RegisteredServiceAccessStrategyAuditableEnforcerTests;
import org.apereo.cas.services.util.RegisteredServiceNoOpCipherExecutorTests;
import org.apereo.cas.services.util.RegisteredServiceYamlHttpMessageConverterTests;

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
    DefaultRegisteredServicePropertyTests.class,
    CasAddonsRegisteredServicesJsonSerializerTests.class,
    RegisteredServiceAccessStrategyAuditableEnforcerTests.class,
    RegisteredServiceReverseMappedRegexAttributeFilterTests.class,
    RegisteredServiceYamlHttpMessageConverterTests.class,
    RegisteredServiceNoOpCipherExecutorTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
