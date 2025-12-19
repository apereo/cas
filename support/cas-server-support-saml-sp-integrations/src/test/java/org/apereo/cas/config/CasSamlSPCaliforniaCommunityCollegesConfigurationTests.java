package org.apereo.cas.config;

import module java.base;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPCaliforniaCommunityCollegesConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.cccco.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.cccco.name-id-attribute=cn",
    "cas.saml-sp.cccco.name-id-format=transient"
})
class CasSamlSPCaliforniaCommunityCollegesConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
