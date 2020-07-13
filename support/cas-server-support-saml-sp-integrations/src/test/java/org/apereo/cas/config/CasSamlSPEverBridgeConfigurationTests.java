package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPEverBridgeConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.saml-sp.ever-bridge.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.ever-bridge.name-id-attribute=cn",
    "cas.saml-sp.ever-bridge.name-id-format=transient"
})
public class CasSamlSPEverBridgeConfigurationTests extends BaseCasSamlSPConfigurationTests {

}
