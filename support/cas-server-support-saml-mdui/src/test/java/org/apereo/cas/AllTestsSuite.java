package org.apereo.cas;

import org.apereo.cas.support.saml.mdui.web.flow.SamlMetadataUIParserActionTests;
import org.apereo.cas.support.saml.mdui.web.flow.SamlMetadataUIParserDynamicActionTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite to run all SAML tests.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({SamlMetadataUIParserActionTests.class, SamlMetadataUIParserDynamicActionTests.class})
public class AllTestsSuite {
}
