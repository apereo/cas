package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPWebexConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")

@TestPropertySource(properties = {
    "cas.saml-sp.webex.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.webex.name-id-attribute=cn",
    "cas.saml-sp.webex.name-id-format=transient"
})
public class CasSamlSPWebexConfigurationTests extends BaseCasSamlSPConfigurationTests {
}
