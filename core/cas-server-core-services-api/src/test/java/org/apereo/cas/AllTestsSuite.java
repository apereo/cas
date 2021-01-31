package org.apereo.cas;

import org.apereo.cas.services.CasYamlHttpMessageConverterTests;
import org.apereo.cas.services.DefaultRegisteredServicePropertyTests;
import org.apereo.cas.services.NeverRegisteredServiceSingleSignOnParticipationPolicyTests;
import org.apereo.cas.services.util.RegisteredServiceAccessStrategyAuditableEnforcerTests;
import org.apereo.cas.services.util.RegisteredServiceNoOpCipherExecutorTests;

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
    RegisteredServiceAccessStrategyAuditableEnforcerTests.class,
    CasYamlHttpMessageConverterTests.class,
    NeverRegisteredServiceSingleSignOnParticipationPolicyTests.class,
    RegisteredServiceNoOpCipherExecutorTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
