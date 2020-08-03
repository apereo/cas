package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPOpenAthensConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.saml-sp.open-athens.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.open-athens.name-id-attribute=cn",
    "cas.saml-sp.open-athens.name-id-format=transient"
})
public class CasSamlSPOpenAthensConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
