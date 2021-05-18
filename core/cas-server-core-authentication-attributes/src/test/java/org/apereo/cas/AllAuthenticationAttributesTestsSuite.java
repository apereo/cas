package org.apereo.cas;

import org.apereo.cas.authentication.DefaultAuthenticationAttributeReleasePolicyTests;
import org.apereo.cas.services.ChainingAttributeReleasePolicyTests;
import org.apereo.cas.services.DefaultRegisteredServiceAcceptableUsagePolicyTests;
import org.apereo.cas.services.DenyAllAttributeReleasePolicyTests;
import org.apereo.cas.services.GroovyScriptAttributeReleasePolicyTests;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyTests;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicyTests;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicyTests;
import org.apereo.cas.services.ReturnMappedAttributeReleasePolicyTests;
import org.apereo.cas.services.ReturnRestfulAttributeReleasePolicyTests;
import org.apereo.cas.services.ScriptedRegisteredServiceAttributeReleasePolicyTests;
import org.apereo.cas.services.consent.ChainingRegisteredServiceConsentPolicyTests;
import org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicyTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * This is {@link AllAuthenticationAttributesTestsSuite}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SelectClasses({
    DenyAllAttributeReleasePolicyTests.class,
    GroovyScriptAttributeReleasePolicyTests.class,
    RegisteredServiceAttributeReleasePolicyTests.class,
    ReturnAllAttributeReleasePolicyTests.class,
    ReturnMappedAttributeReleasePolicyTests.class,
    ReturnRestfulAttributeReleasePolicyTests.class,
    DefaultAuthenticationAttributeReleasePolicyTests.class,
    ScriptedRegisteredServiceAttributeReleasePolicyTests.class,
    ReturnAllowedAttributeReleasePolicyTests.class,
    ChainingAttributeReleasePolicyTests.class,
    ChainingRegisteredServiceConsentPolicyTests.class,
    DefaultRegisteredServiceAcceptableUsagePolicyTests.class,
    DefaultRegisteredServiceConsentPolicyTests.class
})
@Suite
public class AllAuthenticationAttributesTestsSuite {
}
