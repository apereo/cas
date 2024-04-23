package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPAmazonConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.amazon.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.amazon.name-id-attribute=cn",
    "cas.saml-sp.amazon.name-id-format=transient"
})
class CasSamlSPAmazonConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
