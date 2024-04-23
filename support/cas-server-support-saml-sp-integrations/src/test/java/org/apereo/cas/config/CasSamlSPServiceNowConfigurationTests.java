package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPServiceNowConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.service-now.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.service-now.name-id-attribute=cn",
    "cas.saml-sp.service-now.name-id-format=transient"
})
class CasSamlSPServiceNowConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
