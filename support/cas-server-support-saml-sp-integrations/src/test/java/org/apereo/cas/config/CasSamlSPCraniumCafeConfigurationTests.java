package org.apereo.cas.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasSamlSPCraniumCafeConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@Import(CasSamlSPCraniumCafeConfiguration.class)
public class CasSamlSPCraniumCafeConfigurationTests extends BaseCasSamlSPConfigurationTests {
    @BeforeAll
    public static void beforeAll() {
        SERVICE_PROVIDER = "craniumCafe";
    }

    @Override
    protected String getServiceProviderId() {
        return casProperties.getSamlSp().getCraniumCafe().getEntityIds().get(0);
    }
}
