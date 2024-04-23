package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPYujaConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.yuja.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.yuja.name-id-attribute=cn",
    "cas.saml-sp.yuja.name-id-format=transient"
})
class CasSamlSPYujaConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
