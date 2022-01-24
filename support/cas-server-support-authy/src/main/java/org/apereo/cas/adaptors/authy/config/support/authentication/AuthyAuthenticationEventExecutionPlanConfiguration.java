package org.apereo.cas.adaptors.authy.config.support.authentication;

import org.apereo.cas.adaptors.authy.AuthyAuthenticationHandler;
import org.apereo.cas.adaptors.authy.AuthyClientInstance;
import org.apereo.cas.adaptors.authy.AuthyMultifactorAuthenticationProvider;
import org.apereo.cas.adaptors.authy.AuthyTokenCredential;
import org.apereo.cas.adaptors.authy.web.flow.AuthyAuthenticationRegistrationWebflowAction;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.handler.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;

import com.authy.AuthyApiClient;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.execution.Action;

import java.net.URL;

/**
 * This is {@link AuthyAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnProperty(prefix = "cas.authn.mfa.authy", name = "api-key")
@Configuration(value = "AuthyAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
public class AuthyAuthenticationEventExecutionPlanConfiguration {

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @ConditionalOnMissingBean(name = "authyClientInstance")
    public AuthyClientInstance authyClientInstance(final CasConfigurationProperties casProperties) throws Exception {
        val properties = casProperties.getAuthn().getMfa().getAuthy();
        val authyUrl = StringUtils.defaultIfBlank(properties.getApiUrl(), AuthyApiClient.DEFAULT_API_URI);
        val url = new URL(authyUrl);
        val testFlag = url.getProtocol().equalsIgnoreCase("http");
        val authyClient = new AuthyApiClient(properties.getApiKey(), authyUrl, testFlag);
        return new AuthyClientInstance(authyClient, properties);
    }

    @ConditionalOnMissingBean(name = "authyAuthenticationHandler")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public AuthenticationHandler authyAuthenticationHandler(
        final CasConfigurationProperties casProperties,
        @Qualifier("authyPrincipalFactory")
        final PrincipalFactory authyPrincipalFactory,
        @Qualifier("authyClientInstance")
        final AuthyClientInstance authyClientInstance,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        val forceVerification = authy.isForceVerification();
        return new AuthyAuthenticationHandler(authy.getName(), servicesManager, authyPrincipalFactory, authyClientInstance, forceVerification, authy.getOrder());
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
        final CasConfigurationProperties casProperties,
        @Qualifier("authyBypassEvaluator")
        final MultifactorAuthenticationProviderBypassEvaluator authyBypassEvaluator,
        @Qualifier("failureModeEvaluator")
        final MultifactorAuthenticationFailureModeEvaluator failureModeEvaluator) {
        val p = new AuthyMultifactorAuthenticationProvider();
        p.setBypassEvaluator(authyBypassEvaluator);
        val authy = casProperties.getAuthn().getMfa().getAuthy();
        p.setFailureMode(authy.getFailureMode());
        p.setFailureModeEvaluator(failureModeEvaluator);
        p.setOrder(authy.getRank());
        p.setId(authy.getId());
        return p;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationMetaDataPopulator authyAuthenticationMetaDataPopulator(
        final CasConfigurationProperties casProperties,
        @Qualifier("authyAuthenticationHandler")
        final AuthenticationHandler authyAuthenticationHandler,
        @Qualifier("authyAuthenticatorMultifactorAuthenticationProvider")
        final MultifactorAuthenticationProvider authyAuthenticatorMultifactorAuthenticationProvider) {
        return new AuthenticationContextAttributeMetaDataPopulator(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute(), authyAuthenticationHandler,
            authyAuthenticatorMultifactorAuthenticationProvider.getId());
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public Action authyAuthenticationRegistrationWebflowAction(
        @Qualifier("authyClientInstance")
        final AuthyClientInstance authyClientInstance) {
        return new AuthyAuthenticationRegistrationWebflowAction(authyClientInstance);
    }

    @ConditionalOnMissingBean(name = "authyAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationEventExecutionPlanConfigurer authyAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("authyAuthenticationHandler")
        final AuthenticationHandler authyAuthenticationHandler,
        @Qualifier("authyAuthenticationMetaDataPopulator")
        final AuthenticationMetaDataPopulator authyAuthenticationMetaDataPopulator) {
        return plan -> {
            plan.registerAuthenticationHandler(authyAuthenticationHandler);
            plan.registerAuthenticationMetadataPopulator(authyAuthenticationMetaDataPopulator);
            plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(AuthyTokenCredential.class));
        };
    }
}
