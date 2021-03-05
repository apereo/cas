package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPCaliforniaCommunityCollegesConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.saml-sp.cccco.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.cccco.name-id-attribute=cn",
    "cas.saml-sp.cccco.name-id-format=transient"
})
public class CasSamlSPCaliforniaCommunityCollegesConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
