package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPGartnerConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")

@TestPropertySource(properties = {
    "cas.saml-sp.gartner.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.gartner.name-id-attribute=cn",
    "cas.saml-sp.gartner.name-id-format=transient"
})
public class CasSamlSPGartnerConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
