package org.apereo.cas;

import org.apereo.cas.services.DenyAllAttributeReleasePolicyTests;
import org.apereo.cas.services.GroovyScriptAttributeReleasePolicyTests;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyTests;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicyTests;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicyTests;
import org.apereo.cas.services.ReturnMappedAttributeReleasePolicyTests;
import org.apereo.cas.services.ReturnRestfulAttributeReleasePolicyTests;
import org.apereo.cas.services.ScriptedRegisteredServiceAttributeReleasePolicyTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * This is {@link AllAuthenticationAttributesTestSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    DenyAllAttributeReleasePolicyTests.class,
    GroovyScriptAttributeReleasePolicyTests.class,
    RegisteredServiceAttributeReleasePolicyTests.class,
    ReturnAllAttributeReleasePolicyTests.class,
    ReturnMappedAttributeReleasePolicyTests.class,
    ReturnRestfulAttributeReleasePolicyTests.class,
    ScriptedRegisteredServiceAttributeReleasePolicyTests.class,
    ReturnAllowedAttributeReleasePolicyTests.class
})
public class AllAuthenticationAttributesTestSuite {
}
