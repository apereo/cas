package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.api.AuthenticationRiskEvaluator;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlan;
import org.apereo.cas.audit.AuditTrailRecordResolutionPlanConfigurer;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.resolvers.EchoingPrincipalResolver;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.consent.ConsentDecisionBuilder;
import org.apereo.cas.consent.ConsentRepository;
import org.apereo.cas.consent.InMemoryConsentRepository;
import org.apereo.cas.heimdall.authorizer.repository.AuthorizableResourceRepository;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlan;
import org.apereo.cas.mcp.CasMcpEvaluationContextFactory;
import org.apereo.cas.mcp.audit.CasMcpAuditor;
import org.apereo.cas.mcp.audit.CasMcpOperationAuditActionResolver;
import org.apereo.cas.mcp.audit.CasMcpOperationAuditResourceResolver;
import org.apereo.cas.mcp.resources.CasSettingResources;
import org.apereo.cas.mcp.tools.CasAuthenticationRiskTools;
import org.apereo.cas.mcp.tools.CasConsentTools;
import org.apereo.cas.mcp.tools.CasDelegatedAuthenticationTools;
import org.apereo.cas.mcp.tools.CasHeimdallTools;
import org.apereo.cas.mcp.tools.CasInterruptTools;
import org.apereo.cas.mcp.tools.CasOidcTools;
import org.apereo.cas.mcp.tools.CasOneTimeTokenTools;
import org.apereo.cas.mcp.tools.CasPasswordlessTools;
import org.apereo.cas.mcp.tools.CasSamlIdPMetadataTools;
import org.apereo.cas.mcp.tools.CasSurrogateTools;
import org.apereo.cas.mcp.tools.CasTrustedDeviceTools;
import org.apereo.cas.mcp.tools.CasWebAuthnTools;
import org.apereo.cas.metadata.CasConfigurationMetadataRepository;
import org.apereo.cas.oidc.issuer.OidcIssuerService;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
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
            assertEquals(0, result.count());
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
                assertEquals("Attribute consent is not configured", result.reason());
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
            assertEquals("The SAML2 identity provider is not configured", result.reason());
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
            assertEquals("The OpenID Connect keystore is not available", result.reason());
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
            assertEquals("Multifactor authentication trusted devices are not configured", result.reason());
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
            assertEquals("Surrogate authentication is not configured", result.reason());
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
            assertEquals("Authentication interrupts are not configured", result.reason());
        });
    }

    @Test
    void verifyEvaluationContextWithoutAttributeRepositories() {
        val serviceFactory = mock(ServiceFactory.class);
        when(serviceFactory.createService(anyString())).thenReturn(mock(WebApplicationService.class));
        val factory = new CasMcpEvaluationContextFactory(mock(ServicesManager.class), serviceFactory, new EchoingPrincipalResolver());
        val context = factory.build("casuser", "https://mcp.example.org");
        assertEquals("casuser", context.principal().getId());
        assertEquals("casuser", context.authentication().getPrincipal().getId());
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
            assertEquals("Risk-based authentication is not configured", result.reason());
        });
    }

    @Test
    void verifyOneTimeTokenToolsActivation() {
        val runner = new ApplicationContextRunner()
            .withUserConfiguration(CasMcpAutoConfiguration.CasMcpOneTimeTokenConfiguration.class);
        runner.run(context -> assertTrue(context.containsBean("casOneTimeTokenTools")));
        runner.withClassLoader(new FilteredClassLoader(OneTimeTokenCredentialRepository.class))
            .run(context -> assertFalse(context.containsBean("casOneTimeTokenTools")));
        runner.withPropertyValues("CasFeatureModule.AI.mcp-otp.enabled=false")
            .run(context -> assertFalse(context.containsBean("casOneTimeTokenTools")));
        runner.run(context -> {
            val result = context.getBean(CasOneTimeTokenTools.class).findOneTimeTokenAccounts("casuser");
            assertEquals("One-time password multifactor authentication is not configured", result.reason());
        });
    }

    @Test
    void verifyWebAuthnToolsActivation() {
        val runner = new ApplicationContextRunner()
            .withUserConfiguration(CasMcpAutoConfiguration.CasMcpWebAuthnConfiguration.class);
        runner.run(context -> assertTrue(context.containsBean("casWebAuthnTools")));
        runner.withClassLoader(new FilteredClassLoader(WebAuthnCredentialRepository.class))
            .run(context -> assertFalse(context.containsBean("casWebAuthnTools")));
        runner.withPropertyValues("CasFeatureModule.AI.mcp-webauthn.enabled=false")
            .run(context -> assertFalse(context.containsBean("casWebAuthnTools")));
        runner.run(context -> {
            val result = context.getBean(CasWebAuthnTools.class).findWebAuthnDevices("casuser");
            assertEquals("WebAuthn multifactor authentication is not configured", result.reason());
        });
    }

    @Test
    void verifyPasswordlessToolsActivation() {
        val runner = new ApplicationContextRunner()
            .withUserConfiguration(CasMcpAutoConfiguration.CasMcpPasswordlessConfiguration.class);
        runner.run(context -> assertTrue(context.containsBean("casPasswordlessTools")));
        runner.withClassLoader(new FilteredClassLoader(PasswordlessUserAccountStore.class))
            .run(context -> assertFalse(context.containsBean("casPasswordlessTools")));
        runner.withPropertyValues("CasFeatureModule.AI.mcp-passwordless.enabled=false")
            .run(context -> assertFalse(context.containsBean("casPasswordlessTools")));
        runner.run(context -> assertEquals("Passwordless authentication is not configured",
            context.getBean(CasPasswordlessTools.class).findPasswordlessAccount("casuser").reason()));
    }

    @Test
    void verifyDelegatedAuthenticationToolsActivation() {
        val runner = new ApplicationContextRunner()
            .withUserConfiguration(CasMcpAutoConfiguration.CasMcpDelegatedAuthenticationConfiguration.class)
            .withBean("casMcpEvaluationContextFactory", CasMcpEvaluationContextFactory.class, () -> mock(CasMcpEvaluationContextFactory.class));
        runner.run(context -> assertTrue(context.containsBean("casDelegatedAuthenticationTools")));
        runner.withClassLoader(new FilteredClassLoader(DelegatedIdentityProviders.class))
            .run(context -> assertFalse(context.containsBean("casDelegatedAuthenticationTools")));
        runner.withPropertyValues("CasFeatureModule.AI.mcp-delegated.enabled=false")
            .run(context -> assertFalse(context.containsBean("casDelegatedAuthenticationTools")));
        runner.run(context -> assertEquals("Delegated authentication is not configured",
            context.getBean(CasDelegatedAuthenticationTools.class).listIdentityProviders().reason()));
    }

    @Test
    void verifyHeimdallToolsActivation() {
        val runner = new ApplicationContextRunner()
            .withUserConfiguration(CasMcpAutoConfiguration.CasMcpHeimdallConfiguration.class);
        runner.run(context -> assertTrue(context.containsBean("casHeimdallTools")));
        runner.withClassLoader(new FilteredClassLoader(AuthorizableResourceRepository.class))
            .run(context -> assertFalse(context.containsBean("casHeimdallTools")));
        runner.withPropertyValues("CasFeatureModule.AI.mcp-heimdall.enabled=false")
            .run(context -> assertFalse(context.containsBean("casHeimdallTools")));
        runner.run(context -> assertEquals("Heimdall is not configured",
            context.getBean(CasHeimdallTools.class).findAuthorizableResources("API").reason()));
    }

    @Test
    void verifySettingResourcesActivation() {
        val runner = new ApplicationContextRunner()
            .withUserConfiguration(CasMcpAutoConfiguration.CasMcpSettingsConfiguration.class);
        runner.run(context -> assertTrue(context.containsBean("casSettingResources") && context.containsBean("casSettingTools")));
        runner.withClassLoader(new FilteredClassLoader(CasConfigurationMetadataRepository.class))
            .run(context -> assertFalse(context.containsBean("casSettingResources")));
        runner.withPropertyValues("CasFeatureModule.AI.mcp-settings.enabled=false")
            .run(context -> assertFalse(context.containsBean("casSettingResources") || context.containsBean("casSettingTools")));
        runner.run(context -> assertTrue(context.getBean(CasSettingResources.class).describeSetting("cas.server.name")
            .contains("The CAS settings catalog is not available")));
    }

    @Test
    void verifyAuditConfigurationActivation() {
        val runner = new ApplicationContextRunner()
            .withUserConfiguration(CasMcpAutoConfiguration.CasMcpAuditConfiguration.class);
        runner.run(context -> {
            assertTrue(context.containsBean("casMcpAuditor"));
            assertTrue(context.containsBean("casMcpAuditAdvisor"));
            val plan = mock(AuditTrailRecordResolutionPlan.class);
            context.getBean("casMcpAuditTrailRecordResolutionPlanConfigurer", AuditTrailRecordResolutionPlanConfigurer.class)
                .configureAuditTrailRecordResolutionPlan(plan);
            verify(plan).registerAuditActionResolver(eq(CasMcpAuditor.AUDIT_ACTION_RESOLVER), any(CasMcpOperationAuditActionResolver.class));
            verify(plan).registerAuditResourceResolver(eq(CasMcpAuditor.AUDIT_RESOURCE_RESOLVER), any(CasMcpOperationAuditResourceResolver.class));
        });
        runner.withPropertyValues("CasFeatureModule.AI.mcp-audit.enabled=false")
            .run(context -> {
                assertFalse(context.containsBean("casMcpAuditor"));
                assertFalse(context.containsBean("casMcpAuditAdvisor"));
            });
    }
}
