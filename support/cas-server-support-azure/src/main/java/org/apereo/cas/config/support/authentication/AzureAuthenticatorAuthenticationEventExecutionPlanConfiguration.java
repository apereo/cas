package org.apereo.cas.config.support.authentication;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.phonefactor.pfsdk.PFAuth;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.azure.AzureAuthenticatorAuthenticationHandler;
import org.apereo.cas.adaptors.azure.AzureAuthenticatorAuthenticationRequestBuilder;
import org.apereo.cas.adaptors.azure.AzureAuthenticatorMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.azure.AzureAuthenticatorTokenCredential;
import org.apereo.cas.adaptors.azure.web.flow.AzureAuthenticatorGenerateTokenAction;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderBypass;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.AzureMultifactorProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * This is {@link AzureAuthenticatorAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration("azureAuthenticatorAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class AzureAuthenticatorAuthenticationEventExecutionPlanConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Bean
    @SneakyThrows
    public PFAuth azureAuthenticatorInstance() {
        final AzureMultifactorProperties azure = casProperties.getAuthn().getMfa().getAzure();
        final File cfg = new File(azure.getConfigDir());
        if (!cfg.exists() || !cfg.isDirectory()) {
            throw new FileNotFoundException(cfg.getAbsolutePath() + " does not exist or is not a directory");
        }
        final PFAuth pf = new PFAuth();
        pf.setDebug(true);
        pf.setAllowInternationalCalls(azure.isAllowInternationalCalls());

        final String dir = StringUtils.appendIfMissing(azure.getConfigDir(), "/");
        pf.initialize(dir, azure.getPrivateKeyPassword());
        return pf;
    }

    @Bean
    public AzureAuthenticatorAuthenticationRequestBuilder azureAuthenticationRequestBuilder() {
        final AzureMultifactorProperties azure = casProperties.getAuthn().getMfa().getAzure();
        return new AzureAuthenticatorAuthenticationRequestBuilder(
            azure.getPhoneAttributeName(),
            azure.getMode());
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler azureAuthenticatorAuthenticationHandler() {
        return new AzureAuthenticatorAuthenticationHandler(casProperties.getAuthn().getMfa().getAzure().getName(),
            servicesManager, azurePrincipalFactory(), azureAuthenticatorInstance(), azureAuthenticationRequestBuilder());
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProviderBypass azureBypassEvaluator() {
        return MultifactorAuthenticationUtils.newMultifactorAuthenticationProviderBypass(casProperties.getAuthn().getMfa().getAzure().getBypass());
    }

    @Bean
    @RefreshScope
    public MultifactorAuthenticationProvider azureAuthenticatorAuthenticationProvider() {
        final AzureMultifactorProperties azure = casProperties.getAuthn().getMfa().getAzure();
        final AzureAuthenticatorMultifactorAuthenticationProvider p = new AzureAuthenticatorMultifactorAuthenticationProvider();
        p.setBypassEvaluator(azureBypassEvaluator());
        p.setFailureMode(casProperties.getAuthn().getMfa().getAzure().getFailureMode());
        p.setOrder(azure.getRank());
        p.setId(azure.getId());
        return p;
    }

    @Bean
    @RefreshScope
    public AuthenticationMetaDataPopulator azureAuthenticatorAuthenticationMetaDataPopulator() {
        return new AuthenticationContextAttributeMetaDataPopulator(
            casProperties.getAuthn().getMfa().getAuthenticationContextAttribute(),
            azureAuthenticatorAuthenticationHandler(),
            azureAuthenticatorAuthenticationProvider().getId()
        );
    }

    @Bean
    @RefreshScope
    public Action azureGenerateTokenAction() {
        final AzureMultifactorProperties azure = casProperties.getAuthn().getMfa().getAzure();
        return new AzureAuthenticatorGenerateTokenAction(azure.getMode());
    }

    @ConditionalOnMissingBean(name = "azurePrincipalFactory")
    @Bean
    public PrincipalFactory azurePrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "azureAuthenticatorAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer azureAuthenticatorAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            if (StringUtils.isNotBlank(casProperties.getAuthn().getMfa().getAzure().getConfigDir())) {
                plan.registerAuthenticationHandler(azureAuthenticatorAuthenticationHandler());
                plan.registerMetadataPopulator(azureAuthenticatorAuthenticationMetaDataPopulator());
                plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(AzureAuthenticatorTokenCredential.class));
            }
        };
    }
}
