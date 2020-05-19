package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPWebAdvisorConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")

@TestPropertySource(properties = {
    "cas.saml-sp.web-advisor.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.web-advisor.name-id-attribute=cn",
    "cas.saml-sp.web-advisor.name-id-format=transient"
})
public class CasSamlSPWebAdvisorConfigurationTests extends BaseCasSamlSPConfigurationTests {

}
