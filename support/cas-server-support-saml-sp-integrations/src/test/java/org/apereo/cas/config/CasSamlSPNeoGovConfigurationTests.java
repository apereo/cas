package org.apereo.cas.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasSamlSPNeoGovConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@Import(CasSamlSPNeoGovConfiguration.class)
public class CasSamlSPNeoGovConfigurationTests extends BaseCasSamlSPConfigurationTests {
    @BeforeAll
    public static void beforeAll() {
        SERVICE_PROVIDER = "neoGov";
    }

    @Override
    protected String getServiceProviderId() {
        return casProperties.getSamlSp().getNeoGov().getEntityIds().get(0);
    }
}
