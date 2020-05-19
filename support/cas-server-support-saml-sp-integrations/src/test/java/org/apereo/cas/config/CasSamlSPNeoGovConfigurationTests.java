package org.apereo.cas.config;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CasSamlSPNeoGovConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.saml-sp.neo-gov.metadata=classpath:/metadata/sp-metadata.xml",
    "cas.saml-sp.neo-gov.name-id-attribute=cn",
    "cas.saml-sp.neo-gov.name-id-format=transient"
})
public class CasSamlSPNeoGovConfigurationTests extends BaseCasSamlSPConfigurationTests {

    @Override
    protected String getServiceProviderId() {
        return casProperties.getSamlSp().getNeoGov().getEntityIds().get(0);
    }
}
