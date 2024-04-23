package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPEgnyteConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.egnyte.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.egnyte.name-id-attribute=cn",
    "cas.saml-sp.egnyte.name-id-format=transient"
})
class CasSamlSPEgnyteConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
