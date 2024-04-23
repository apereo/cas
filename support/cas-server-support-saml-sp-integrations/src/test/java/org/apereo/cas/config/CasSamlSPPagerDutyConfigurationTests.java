package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPPagerDutyConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.pager-duty.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.pager-duty.name-id-attribute=cn",
    "cas.saml-sp.pager-duty.name-id-format=transient"
})
class CasSamlSPPagerDutyConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
