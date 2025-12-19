package org.apereo.cas.config;

import module java.base;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPSecuringTheHumanConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.sans-sth.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.sans-sth.name-id-attribute=cn",
    "cas.saml-sp.sans-sth.name-id-format=transient"
})
class CasSamlSPSecuringTheHumanConfigurationTests extends BaseCasSamlSPConfigurationTests {


}
