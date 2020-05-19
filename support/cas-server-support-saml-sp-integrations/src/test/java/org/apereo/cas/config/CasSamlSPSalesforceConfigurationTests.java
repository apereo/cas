package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPSalesforceConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")

@TestPropertySource(properties = {
    "cas.saml-sp.salesforce.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.salesforce.name-id-attribute=cn",
    "cas.saml-sp.salesforce.name-id-format=transient"
})
public class CasSamlSPSalesforceConfigurationTests extends BaseCasSamlSPConfigurationTests {

}
