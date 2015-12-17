package org.jasig.cas.support.saml;

import org.jasig.cas.support.saml.web.flow.mdui.SamlMetadataUIParserActionTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite to run all SAML tests.
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        SamlMetadataUIParserActionTests.class,
})
public final class AllTestsSuite {
}
