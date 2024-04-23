package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPSafariOnlineConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.safari-online.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.safari-online.name-id-attribute=cn",
    "cas.saml-sp.safari-online.name-id-format=transient"
})
class CasSamlSPSafariOnlineConfigurationTests extends BaseCasSamlSPConfigurationTests {

}
