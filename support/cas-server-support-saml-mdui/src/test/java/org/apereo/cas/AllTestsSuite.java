package org.apereo.cas;

import org.apereo.cas.support.saml.mdui.web.flow.SamlMetadataUIParserActionTests;
import org.apereo.cas.support.saml.mdui.web.flow.SamlMetadataUIParserDynamicActionTests;

import org.junit.platform.suite.api.SelectClasses;

/**
 * Test suite to run all SAML tests.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@SelectClasses({SamlMetadataUIParserActionTests.class, SamlMetadataUIParserDynamicActionTests.class})
public class AllTestsSuite {
}
