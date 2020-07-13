package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPNewRelicConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.saml-sp.new-relic.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.new-relic.name-id-attribute=cn",
    "cas.saml-sp.new-relic.name-id-format=transient"
})
public class CasSamlSPNewRelicConfigurationTests extends BaseCasSamlSPConfigurationTests {

}
