package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPAdobeCreativeCloudConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.adobe-cloud.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.adobe-cloud.name-id-attribute=cn",
    "cas.saml-sp.adobe-cloud.name-id-format=transient"
})
class CasSamlSPAdobeCreativeCloudConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
