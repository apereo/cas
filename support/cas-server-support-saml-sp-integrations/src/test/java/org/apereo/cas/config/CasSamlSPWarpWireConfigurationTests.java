package org.apereo.cas.config;

import module java.base;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPWarpWireConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.warp-wire.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.warp-wire.name-id-attribute=cn",
    "cas.saml-sp.warp-wire.name-id-format=transient"
})
class CasSamlSPWarpWireConfigurationTests extends BaseCasSamlSPConfigurationTests {

}
