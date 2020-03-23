package org.apereo.cas.web.flow.config;

import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.web.flow.SpnegoCredentialsAction;
import org.apereo.cas.web.flow.SpnegoNegotiateCredentialsAction;
import org.apereo.cas.web.flow.client.BaseSpnegoKnownClientSystemsFilterAction;
import org.apereo.cas.web.flow.client.HostNameSpnegoKnownClientSystemsFilterAction;
import org.apereo.cas.web.flow.client.LdapSpnegoKnownClientSystemsFilterAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import lombok.val;
import org.ldaptive.SearchOperation;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.webflow.execution.Action;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link SpnegoWebflowActionsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "spnegoWebflowActionsConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SpnegoWebflowActionsConfiguration {

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private ObjectProvider<AdaptiveAuthenticationPolicy> adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private ObjectProvider<CasWebflowEventResolver> serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private ObjectProvider<CasDelegatingWebflowEventResolver> initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @RefreshScope
    public Action spnego() {
        val spnegoProperties = casProperties.getAuthn().getSpnego();
        return new SpnegoCredentialsAction(initialAuthenticationAttemptWebflowEventResolver.getObject(),
            serviceTicketRequestWebflowEventResolver.getObject(),
            adaptiveAuthenticationPolicy.getObject(),
            spnegoProperties.isNtlm(),
            spnegoProperties.isSend401OnAuthenticationFailure());
    }

    @Bean
    @RefreshScope
    public Action negociateSpnego() {
        val spnegoProperties = casProperties.getAuthn().getSpnego();
        val supportedBrowsers = Stream.of(spnegoProperties.getSupportedBrowsers().split(",")).collect(Collectors.toList());
        return new SpnegoNegotiateCredentialsAction(supportedBrowsers, spnegoProperties.isNtlm(), spnegoProperties.isMixedModeAuthentication());
    }

    @Bean
    @RefreshScope
    public Action baseSpnegoClientAction() {
        val spnegoProperties = casProperties.getAuthn().getSpnego();
        return new BaseSpnegoKnownClientSystemsFilterAction(RegexUtils.createPattern(spnegoProperties.getIpsToCheckPattern()),
            spnegoProperties.getAlternativeRemoteHostAttribute(),
            Beans.newDuration(spnegoProperties.getDnsTimeout()).toMillis());
    }

    @Bean
    @RefreshScope
    public Action hostnameSpnegoClientAction() {
        val spnegoProperties = casProperties.getAuthn().getSpnego();
        return new HostNameSpnegoKnownClientSystemsFilterAction(RegexUtils.createPattern(spnegoProperties.getIpsToCheckPattern()),
            spnegoProperties.getAlternativeRemoteHostAttribute(),
            Beans.newDuration(spnegoProperties.getDnsTimeout()).toMillis(),
            spnegoProperties.getHostNamePatternString());
    }

    @Lazy
    @Bean
    @RefreshScope
    public Action ldapSpnegoClientAction() {
        val spnegoProperties = casProperties.getAuthn().getSpnego();
        val connectionFactory = LdapUtils.newLdaptiveConnectionFactory(spnegoProperties.getLdap());
        val filter = LdapUtils.newLdaptiveSearchFilter(spnegoProperties.getLdap().getSearchFilter());

        val searchRequest = LdapUtils.newLdaptiveSearchRequest(spnegoProperties.getLdap().getBaseDn(), filter);
        val searchOperation = new SearchOperation(connectionFactory, searchRequest);
        searchOperation.setTemplate(filter);

        return new LdapSpnegoKnownClientSystemsFilterAction(RegexUtils.createPattern(spnegoProperties.getIpsToCheckPattern()),
            spnegoProperties.getAlternativeRemoteHostAttribute(),
            Beans.newDuration(spnegoProperties.getDnsTimeout()).toMillis(),
            searchOperation,
            spnegoProperties.getSpnegoAttributeName());
    }
}
