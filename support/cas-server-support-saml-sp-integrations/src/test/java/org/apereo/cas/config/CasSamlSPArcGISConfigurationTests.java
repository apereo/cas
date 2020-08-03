package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPArcGISConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.saml-sp.arcGIS.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.arcGIS.name-id-attribute=cn",
    "cas.saml-sp.arcGIS.name-id-format=transient"
})
public class CasSamlSPArcGISConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
