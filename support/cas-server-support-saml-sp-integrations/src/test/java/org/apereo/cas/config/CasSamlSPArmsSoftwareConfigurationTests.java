package org.apereo.cas.config;

import module java.base;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPArmsSoftwareConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = {
    "cas.saml-sp.arms-software.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.arms-software.name-id-attribute=cn",
    "cas.saml-sp.arms-software.name-id-format=transient"
})
class CasSamlSPArmsSoftwareConfigurationTests extends BaseCasSamlSPConfigurationTests {

    @Override
    protected String getServiceProviderId() {
        return casProperties.getSamlSp().getArmsSoftware().getEntityIds().getFirst();
    }
}
