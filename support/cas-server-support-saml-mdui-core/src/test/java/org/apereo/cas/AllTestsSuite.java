package org.apereo.cas;

import org.apereo.cas.support.saml.mdui.DynamicMetadataResolverAdapterTests;
import org.apereo.cas.support.saml.mdui.MetadataUIUtilsTests;
import org.apereo.cas.support.saml.mdui.SamlMetadataUIInfoTests;
import org.apereo.cas.support.saml.mdui.StaticMetadataResolverAdapterTests;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Test suite to run all SAML tests.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses({
    DynamicMetadataResolverAdapterTests.class,
    StaticMetadataResolverAdapterTests.class,
    MetadataUIUtilsTests.class,
    SamlMetadataUIInfoTests.class
})
@Suite
public class AllTestsSuite {
}
