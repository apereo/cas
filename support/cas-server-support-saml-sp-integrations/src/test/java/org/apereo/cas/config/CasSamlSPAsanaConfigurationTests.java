package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPAsanaConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")

@TestPropertySource(properties = {
    "cas.saml-sp.asana.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.asana.name-id-attribute=cn",
    "cas.saml-sp.asana.name-id-format=transient"
})
public class CasSamlSPAsanaConfigurationTests extends BaseCasSamlSPConfigurationTests {

}
