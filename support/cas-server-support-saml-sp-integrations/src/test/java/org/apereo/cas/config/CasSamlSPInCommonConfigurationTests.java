package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPInCommonConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.saml-sp.in-common.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.in-common.name-id-attribute=cn",
    "cas.saml-sp.in-common.name-id-format=transient"
})
public class CasSamlSPInCommonConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
