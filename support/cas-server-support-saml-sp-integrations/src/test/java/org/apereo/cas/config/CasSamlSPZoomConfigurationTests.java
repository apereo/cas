package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPZoomConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")

@TestPropertySource(properties = {
    "cas.saml-sp.zoom.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.zoom.name-id-attribute=cn",
    "cas.saml-sp.zoom.name-id-format=transient"
})
public class CasSamlSPZoomConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
