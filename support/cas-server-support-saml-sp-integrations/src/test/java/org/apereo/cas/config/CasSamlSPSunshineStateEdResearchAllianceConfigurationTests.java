package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPSunshineStateEdResearchAllianceConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.sserca.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.sserca.name-id-attribute=cn",
    "cas.saml-sp.sserca.name-id-format=transient"
})
class CasSamlSPSunshineStateEdResearchAllianceConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
