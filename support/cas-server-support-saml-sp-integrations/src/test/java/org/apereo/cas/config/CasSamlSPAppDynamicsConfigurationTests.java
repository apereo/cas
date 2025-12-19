package org.apereo.cas.config;

import module java.base;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPAppDynamicsConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.app-dynamics.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.app-dynamics.name-id-attribute=cn",
    "cas.saml-sp.app-dynamics.name-id-format=transient"
})
class CasSamlSPAppDynamicsConfigurationTests extends BaseCasSamlSPConfigurationTests {

}
