package org.apereo.cas.adaptors.authy.config.support.authentication;

import com.authy.AuthyApiClient;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.adaptors.authy.AuthyAuthenticationHandler;
import org.apereo.cas.adaptors.authy.config.OktaMfaProperties;
import org.apereo.cas.adaptors.authy.core.AuthyClientInstance;
import org.apereo.cas.adaptors.authy.core.AuthyMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.authy.core.AuthyTokenCredential;
import org.apereo.cas.adaptors.authy.core.DefaultAuthyClientInstance;
import org.apereo.cas.authentication.*;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.handler.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.metadata.MultifactorAuthenticationProviderMetadataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;

import java.net.URL;

/**
 * This is {@link AuthyAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Jérémie POISSON
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@EnableConfigurationProperties({CasConfigurationProperties.class, OktaMfaProperties.class})
//@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authy)
@AutoConfiguration
public class AuthyAuthenticationEventExecutionPlanConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("okta.mfa.cas.token");

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "authyClientInstance")
    public AuthyClientInstance authyClientInstance(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext) throws Exception {

        System.out.println("**************************************");
        System.out.println("CREATE AUTHY CLIENT INSTANCE");
        System.out.println("**************************************");

        return BeanSupplier.of(AuthyClientInstance.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(Unchecked.supplier(() -> {
                val properties = casProperties.getAuthn().getMfa().getAuthy();
                val authyUrl = StringUtils.defaultIfBlank(properties.getApiUrl(), AuthyApiClient.DEFAULT_API_URI);
                val url = new URL(authyUrl);
                val testFlag = url.getProtocol().equalsIgnoreCase("http");
                val authyClient = new AuthyApiClient(properties.getApiKey(), authyUrl, testFlag);
                return new DefaultAuthyClientInstance(authyClient, properties);
            }))
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "authyAuthenticationHandler")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public AuthenticationHandler authyAuthenticationHandler(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("authyPrincipalFactory")
        final PrincipalFactory authyPrincipalFactory,
        @Qualifier("authyAuthenticatorMultifactorAuthenticationProvider")
        final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider,
        @Qualifier("authyClientInstance")
        final AuthyClientInstance authyClientInstance,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) throws Exception {
        return BeanSupplier.of(AuthenticationHandler.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val authy = casProperties.getAuthn().getMfa().getAuthy();
                val forceVerification = authy.isForceVerification();
                return new AuthyAuthenticationHandler(authy.getName(), servicesManager,
                    authyPrincipalFactory, authyClientInstance,
                    forceVerification, authy.getOrder(), multifactorAuthenticationProvider);
            })
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "authyPrincipalFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalFactory authyPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public MultifactorAuthenticationProvider authyAuthenticatorMultifactorAuthenticationProvider(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("authyBypassEvaluator")
        final MultifactorAuthenticationProviderBypassEvaluator authyBypassEvaluator,
        @Qualifier("failureModeEvaluator")
        final MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator) throws Exception {
        return BeanSupplier.of(MultifactorAuthenticationProvider.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val p = new AuthyMultifactorAuthenticationProvider();
                p.setBypassEvaluator(authyBypassEvaluator);
                val authy = casProperties.getAuthn().getMfa().getAuthy();
                p.setFailureMode(authy.getFailureMode());
                p.setFailureModeEvaluator(failureModeEvaluator);
                p.setOrder(authy.getRank());
                p.setId(authy.getId());
                return p;
            })
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "authyMultifactorProviderAuthenticationMetadataPopulator")
    public AuthenticationMetaDataPopulator authyMultifactorProviderAuthenticationMetadataPopulator(
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties,
        @Qualifier("authyAuthenticatorMultifactorAuthenticationProvider")
        final ObjectProvider<MultifactorAuthenticationProvider> multifactorAuthenticationProvider) {
        val authenticationContextAttribute = casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute();
        return new MultifactorAuthenticationProviderMetadataPopulator(authenticationContextAttribute,
            multifactorAuthenticationProvider, servicesManager);
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationMetaDataPopulator authyAuthenticationMetaDataPopulator(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties,
        @Qualifier("authyAuthenticationHandler")
        final AuthenticationHandler authyAuthenticationHandler,
        @Qualifier("authyAuthenticatorMultifactorAuthenticationProvider")
        final MultifactorAuthenticationProvider authyAuthenticatorMultifactorAuthenticationProvider) throws Exception {
        return BeanSupplier.of(AuthenticationMetaDataPopulator.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> new AuthenticationContextAttributeMetaDataPopulator(
                casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute(), authyAuthenticationHandler,
                authyAuthenticatorMultifactorAuthenticationProvider.getId()))
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "authyAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationEventExecutionPlanConfigurer authyAuthenticationEventExecutionPlanConfigurer(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("authyMultifactorProviderAuthenticationMetadataPopulator")
        final AuthenticationMetaDataPopulator authyMultifactorProviderAuthenticationMetadataPopulator,
        @Qualifier("authyAuthenticationHandler")
        final AuthenticationHandler authyAuthenticationHandler,
        @Qualifier("authyAuthenticationMetaDataPopulator")
        final AuthenticationMetaDataPopulator authyAuthenticationMetaDataPopulator) throws Exception {
        return BeanSupplier.of(AuthenticationEventExecutionPlanConfigurer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> plan -> {
                plan.registerAuthenticationHandler(authyAuthenticationHandler);
                plan.registerAuthenticationMetadataPopulator(authyAuthenticationMetaDataPopulator);
                plan.registerAuthenticationMetadataPopulator(authyMultifactorProviderAuthenticationMetadataPopulator);
                plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(AuthyTokenCredential.class));
            })
            .otherwiseProxy()
            .get();
    }
}
