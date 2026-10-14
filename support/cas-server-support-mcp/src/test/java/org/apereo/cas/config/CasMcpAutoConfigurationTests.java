package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.api.AuthenticationRiskEvaluator;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.consent.ConsentDecisionBuilder;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.InMemoryConsentRepository;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlan;
import org.apereo.cas.mcp.CasMcpEvaluationContextFactory;
import org.apereo.cas.mcp.tools.CasAuthenticationRiskTools;
import org.apereo.cas.mcp.tools.CasConsentTools;
import org.apereo.cas.mcp.tools.CasInterruptTools;
import org.apereo.cas.mcp.tools.CasOidcTools;
import org.apereo.cas.mcp.tools.CasSamlIdPMetadataTools;
import org.apereo.cas.mcp.tools.CasSurrogateTools;
import org.apereo.cas.mcp.tools.CasTrustedDeviceTools;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
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

    @Test
    void verifyTrustedDeviceToolsActivation() {
        val runner = new ApplicationContextRunner()
            .withUserConfiguration(CasMcpAutoConfiguration.CasMcpTrustedDevicesConfiguration.class);
        runner.run(context -> assertTrue(context.containsBean("casTrustedDeviceTools")));
        runner.withClassLoader(new FilteredClassLoader(MultifactorAuthenticationTrustStorage.class))
            .run(context -> assertFalse(context.containsBean("casTrustedDeviceTools")));
        runner.withPropertyValues("CasFeatureModule.AI.mcp-trusted-mfa.enabled=false")
            .run(context -> assertFalse(context.containsBean("casTrustedDeviceTools")));
        runner.run(context -> {
            val result = context.getBean(CasTrustedDeviceTools.class).findTrustedDevices("casuser");
            assertEquals("Multifactor authentication trusted devices are not configured", result.get("reason"));
        });
    }

    @Test
    void verifySurrogateToolsActivation() {
        val runner = new ApplicationContextRunner()
            .withUserConfiguration(CasMcpAutoConfiguration.CasMcpSurrogateConfiguration.class)
            .withBean(WebApplicationService.BEAN_NAME_FACTORY, ServiceFactory.class, () -> mock(ServiceFactory.class));
        runner.run(context -> assertTrue(context.containsBean("casSurrogateTools")));
        runner.withClassLoader(new FilteredClassLoader(SurrogateAuthenticationService.class))
            .run(context -> assertFalse(context.containsBean("casSurrogateTools")));
        runner.withPropertyValues("CasFeatureModule.AI.mcp-surrogate.enabled=false")
            .run(context -> assertFalse(context.containsBean("casSurrogateTools")));
        runner.run(context -> {
            val result = context.getBean(CasSurrogateTools.class).findSurrogateAccounts("casuser", null);
            assertEquals("Surrogate authentication is not configured", result.get("reason"));
        });
    }

    @Test
    void verifyInterruptToolsActivation() {
        val runner = new ApplicationContextRunner()
            .withUserConfiguration(CasMcpAutoConfiguration.CasMcpInterruptConfiguration.class)
            .withBean("casMcpEvaluationContextFactory", CasMcpEvaluationContextFactory.class, () -> mock(CasMcpEvaluationContextFactory.class));
        runner.run(context -> assertTrue(context.containsBean("casInterruptTools")));
        runner.withClassLoader(new FilteredClassLoader(InterruptInquiryExecutionPlan.class))
            .run(context -> assertFalse(context.containsBean("casInterruptTools")));
        runner.withPropertyValues("CasFeatureModule.AI.mcp-interrupt.enabled=false")
            .run(context -> assertFalse(context.containsBean("casInterruptTools")));
        runner.run(context -> {
            val result = context.getBean(CasInterruptTools.class).previewInterrupt("casuser", "https://mcp.example.org");
            assertEquals("Authentication interrupts are not configured", result.get("reason"));
        });
    }

    @Test
    void verifyAuthenticationRiskToolsActivation() {
        val runner = new ApplicationContextRunner()
            .withUserConfiguration(CasMcpAutoConfiguration.CasMcpAuthenticationRiskConfiguration.class)
            .withBean("casMcpEvaluationContextFactory", CasMcpEvaluationContextFactory.class, () -> mock(CasMcpEvaluationContextFactory.class));
        runner.run(context -> assertTrue(context.containsBean("casAuthenticationRiskTools")));
        runner.withClassLoader(new FilteredClassLoader(AuthenticationRiskEvaluator.class))
            .run(context -> assertFalse(context.containsBean("casAuthenticationRiskTools")));
        runner.withPropertyValues("CasFeatureModule.AI.mcp-electrofence.enabled=false")
            .run(context -> assertFalse(context.containsBean("casAuthenticationRiskTools")));
        runner.run(context -> {
            val result = context.getBean(CasAuthenticationRiskTools.class)
                .evaluateAuthenticationRisk("casuser", "https://mcp.example.org", "192.0.2.10", "Firefox", null);
            assertEquals("Risk-based authentication is not configured", result.get("reason"));
        });
    }
}
