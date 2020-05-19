package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPBoxConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")

@TestPropertySource(properties = {
    "cas.saml-sp.box.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.box.name-id-attribute=cn",
    "cas.saml-sp.box.name-id-format=transient"
})
public class CasSamlSPBoxConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
