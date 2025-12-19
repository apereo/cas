package org.apereo.cas.config;

import module java.base;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPConfluenceConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.confluence.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.confluence.name-id-attribute=cn",
    "cas.saml-sp.confluence.name-id-format=transient"
})
class CasSamlSPConfluenceConfigurationTests extends BaseCasSamlSPConfigurationTests {

}
