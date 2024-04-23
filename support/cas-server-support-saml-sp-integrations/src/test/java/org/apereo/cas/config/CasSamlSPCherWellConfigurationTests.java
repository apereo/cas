package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPCherWellConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.cher-well.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.cher-well.name-id-attribute=cn",
    "cas.saml-sp.cher-well.name-id-format=transient"
})
class CasSamlSPCherWellConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
