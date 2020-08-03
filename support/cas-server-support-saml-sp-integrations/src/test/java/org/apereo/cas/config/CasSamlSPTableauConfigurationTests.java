package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPTableauConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")

@TestPropertySource(properties = {
    "cas.saml-sp.tableau.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.tableau.name-id-attribute=cn",
    "cas.saml-sp.tableau.name-id-format=transient"
})
public class CasSamlSPTableauConfigurationTests extends BaseCasSamlSPConfigurationTests {

}
