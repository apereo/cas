package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPSecuringTheHumanConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")

@TestPropertySource(properties = {
    "cas.saml-sp.sans-sth.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.sans-sth.name-id-attribute=cn",
    "cas.saml-sp.sans-sth.name-id-format=transient"
})
public class CasSamlSPSecuringTheHumanConfigurationTests extends BaseCasSamlSPConfigurationTests {


}
