package org.apereo.cas;

import org.apereo.cas.support.saml.mdui.DynamicMetadataResolverAdapterTests;
import org.apereo.cas.support.saml.mdui.MetadataUIUtilsTests;
import org.apereo.cas.support.saml.mdui.SamlMetadataUIInfoTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/**
 * Test suite to run all SAML tests.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses({
    DynamicMetadataResolverAdapterTests.class,
    MetadataUIUtilsTests.class,
    SamlMetadataUIInfoTests.class
})
@RunWith(JUnitPlatform.class)
public class AllTestsSuite {
}
