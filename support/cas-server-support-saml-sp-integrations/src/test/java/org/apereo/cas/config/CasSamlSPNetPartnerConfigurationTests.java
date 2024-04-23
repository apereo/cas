package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPNetPartnerConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.net-partner.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.net-partner.name-id-attribute=cn",
    "cas.saml-sp.net-partner.name-id-format=transient"
})
class CasSamlSPNetPartnerConfigurationTests extends BaseCasSamlSPConfigurationTests {

}
