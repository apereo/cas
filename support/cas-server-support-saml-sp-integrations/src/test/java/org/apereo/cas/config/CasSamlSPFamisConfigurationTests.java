package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPFamisConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.saml-sp.famis.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.famis.name-id-attribute=cn",
    "cas.saml-sp.famis.name-id-format=transient"
})
public class CasSamlSPFamisConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
