package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.consent.ConsentDecisionBuilder;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.InMemoryConsentRepository;
import org.apereo.cas.mcp.tools.CasConsentTools;
import org.apereo.cas.mcp.tools.CasOidcTools;
import org.apereo.cas.mcp.tools.CasSamlIdPMetadataTools;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasMcpAutoConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("RestfulApi")
class CasMcpAutoConfigurationTests {
    private static ApplicationContextRunner consentContextRunner() {
        return new ApplicationContextRunner()
            .withUserConfiguration(CasMcpAutoConfiguration.CasMcpConsentConfiguration.class)
            .withBean(ConsentRepository.BEAN_NAME, ConsentRepository.class, InMemoryConsentRepository::new);
    }

    @Test
    void verifyConsentToolsWhenConsentIsAvailable() {
        consentContextRunner().run(context -> {
            assertNull(context.getStartupFailure());
            assertTrue(context.containsBean("casConsentTools"));
            val result = context.getBean(CasConsentTools.class).findConsentDecisions("casuser");
            assertEquals(0, result.get("count"));
        });
    }

    @Test
    void verifyConsentToolsWithoutConsentModule() {
        consentContextRunner()
            .withClassLoader(new FilteredClassLoader(ConsentRepository.class, ConsentDecisionBuilder.class))
            .run(context -> {
                assertNull(context.getStartupFailure());
                assertFalse(context.containsBean("casConsentTools"));
            });
    }

    @Test
    void verifyConsentToolsCanBeDisabled() {
        consentContextRunner()
            .withPropertyValues("CasFeatureModule.AI.mcp-consent.enabled=false")
            .run(context -> {
                assertNull(context.getStartupFailure());
                assertFalse(context.containsBean("casConsentTools"));
            });
    }

    @Test
    void verifyConsentToolsWithoutConsentRepository() {
        new ApplicationContextRunner()
            .withUserConfiguration(CasMcpAutoConfiguration.CasMcpConsentConfiguration.class)
            .run(context -> {
                assertNull(context.getStartupFailure());
                val result = context.getBean(CasConsentTools.class).findConsentDecisions("casuser");
                assertEquals("Attribute consent is not configured", result.get("reason"));
            });
    }

    @Test
    void verifySamlIdPToolsActivation() {
        val runner = new ApplicationContextRunner()
            .withUserConfiguration(CasMcpAutoConfiguration.CasMcpSamlIdPConfiguration.class)
            .withBean(ServicesManager.BEAN_NAME, ServicesManager.class, () -> mock(ServicesManager.class));
        runner.run(context -> assertTrue(context.containsBean("casSamlIdPMetadataTools")));
        runner.withClassLoader(new FilteredClassLoader(SamlRegisteredServiceCachingMetadataResolver.class))
            .run(context -> assertFalse(context.containsBean("casSamlIdPMetadataTools")));
        runner.withPropertyValues("CasFeatureModule.AI.mcp-saml-idp.enabled=false")
            .run(context -> assertFalse(context.containsBean("casSamlIdPMetadataTools")));
        runner.run(context -> {
            val result = context.getBean(CasSamlIdPMetadataTools.class).resolveSamlMetadata("https://sp.example.org");
            assertEquals("The SAML2 identity provider is not configured", result.get("reason"));
        });
    }

    @Test
    void verifyOidcToolsActivation() {
        val runner = new ApplicationContextRunner()
            .withUserConfiguration(CasMcpAutoConfiguration.CasMcpOidcConfiguration.class)
            .withBean(ServicesManager.BEAN_NAME, ServicesManager.class, () -> mock(ServicesManager.class));
        runner.run(context -> assertTrue(context.containsBean("casOidcTools")));
        runner.withClassLoader(new FilteredClassLoader(OidcIssuerService.class))
            .run(context -> assertFalse(context.containsBean("casOidcTools")));
        runner.withClassLoader(new FilteredClassLoader(OidcRegisteredService.class))
            .run(context -> assertFalse(context.containsBean("casOidcTools")));
        runner.withPropertyValues("CasFeatureModule.AI.mcp-oidc.enabled=false")
            .run(context -> assertFalse(context.containsBean("casOidcTools")));
        runner.run(context -> {
            val result = context.getBean(CasOidcTools.class).listJwksKeys();
            assertEquals("The OpenID Connect keystore is not available", result.get("reason"));
        });
    }
}
