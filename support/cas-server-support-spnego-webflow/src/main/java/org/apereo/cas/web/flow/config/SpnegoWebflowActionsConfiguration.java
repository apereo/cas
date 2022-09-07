package org.apereo.cas.web.flow.config;

import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.SpnegoCredentialsAction;
import org.apereo.cas.web.flow.SpnegoNegotiateCredentialsAction;
import org.apereo.cas.web.flow.client.BaseSpnegoKnownClientSystemsFilterAction;
import org.apereo.cas.web.flow.client.HostNameSpnegoKnownClientSystemsFilterAction;
import org.apereo.cas.web.flow.client.LdapSpnegoKnownClientSystemsFilterAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import lombok.val;
import org.ldaptive.SearchOperation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.webflow.execution.Action;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link SpnegoWebflowActionsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SPNEGO)
@AutoConfiguration
public class SpnegoWebflowActionsConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SPNEGO)
    public Action spnego(
        final CasConfigurationProperties casProperties,
        @Qualifier("adaptiveAuthenticationPolicy")
        final AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy,
        @Qualifier("serviceTicketRequestWebflowEventResolver")
        final CasWebflowEventResolver serviceTicketRequestWebflowEventResolver,
        @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
        final CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver) {
        val spnegoProperties = casProperties.getAuthn().getSpnego();
        return new SpnegoCredentialsAction(initialAuthenticationAttemptWebflowEventResolver,
            serviceTicketRequestWebflowEventResolver, adaptiveAuthenticationPolicy,
            spnegoProperties.isSend401OnAuthenticationFailure());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SPNEGO_NEGOTIATE)
    public Action negociateSpnego(final CasConfigurationProperties casProperties) {
        val spnegoProperties = casProperties.getAuthn().getSpnego();
        val supportedBrowsers = Stream.of(spnegoProperties.getSupportedBrowsers().split(",")).collect(Collectors.toList());
        return new SpnegoNegotiateCredentialsAction(supportedBrowsers,
            spnegoProperties.isMixedModeAuthentication());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SPNEGO_CLIENT_BASE)
    public Action baseSpnegoClientAction(final CasConfigurationProperties casProperties) {
        val spnegoProperties = casProperties.getAuthn().getSpnego();
        return new BaseSpnegoKnownClientSystemsFilterAction(
            RegexUtils.createPattern(spnegoProperties.getIpsToCheckPattern()),
            spnegoProperties.getAlternativeRemoteHostAttribute(),
            Beans.newDuration(spnegoProperties.getDnsTimeout()).toMillis());
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SPNEGO_CLIENT_HOSTNAME)
    public Action hostnameSpnegoClientAction(final CasConfigurationProperties casProperties) {
        val spnegoProperties = casProperties.getAuthn().getSpnego();
        return new HostNameSpnegoKnownClientSystemsFilterAction(RegexUtils.createPattern(
            spnegoProperties.getIpsToCheckPattern()), spnegoProperties.getAlternativeRemoteHostAttribute(),
            Beans.newDuration(spnegoProperties.getDnsTimeout()).toMillis(), spnegoProperties.getHostNamePatternString());
    }

    @Configuration(value = "SpnegoLdapWebflowActionsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SPNEGO, module = "ldap")
    public static class SpnegoLdapWebflowActionsConfiguration {
        private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.spnego.ldap.ldap-url");

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_SPNEGO_CLIENT_LDAP)
        public Action ldapSpnegoClientAction(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(Action.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val spnegoProperties = casProperties.getAuthn().getSpnego();
                    val connectionFactory = LdapUtils.newLdaptiveConnectionFactory(spnegoProperties.getLdap());
                    val filter = LdapUtils.newLdaptiveSearchFilter(spnegoProperties.getLdap().getSearchFilter());
                    val searchRequest = LdapUtils.newLdaptiveSearchRequest(spnegoProperties.getLdap().getBaseDn(), filter);
                    val searchOperation = new SearchOperation(connectionFactory, searchRequest);
                    searchOperation.setTemplate(filter);
                    return new LdapSpnegoKnownClientSystemsFilterAction(
                        RegexUtils.createPattern(spnegoProperties.getIpsToCheckPattern()),
                        spnegoProperties.getAlternativeRemoteHostAttribute(),
                        Beans.newDuration(spnegoProperties.getDnsTimeout()).toMillis(),
                        searchOperation, spnegoProperties.getSpnegoAttributeName());
                })
                .otherwiseProxy()
                .get();
        }
    }

}
