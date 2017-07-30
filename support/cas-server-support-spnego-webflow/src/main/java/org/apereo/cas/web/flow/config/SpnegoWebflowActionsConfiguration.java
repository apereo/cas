package org.apereo.cas.web.flow.config;

import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.spnego.SpnegoProperties;

import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.web.flow.SpnegoCredentialsAction;
import org.apereo.cas.web.flow.SpnegoNegociateCredentialsAction;
import org.apereo.cas.web.flow.client.BaseSpnegoKnownClientSystemsFilterAction;
import org.apereo.cas.web.flow.client.HostNameSpnegoKnownClientSystemsFilterAction;
import org.apereo.cas.web.flow.client.LdapSpnegoKnownClientSystemsFilterAction;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.webflow.execution.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link SpnegoWebflowActionsConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("spnegoWebflowActionsConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SpnegoWebflowActionsConfiguration {

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;
    
    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Bean
    @RefreshScope
    public Action spnego() {
        final SpnegoProperties spnegoProperties = casProperties.getAuthn().getSpnego();
        return new SpnegoCredentialsAction(initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy,
                spnegoProperties.isNtlm(),
                spnegoProperties.isSend401OnAuthenticationFailure());
    }

    @Bean
    @RefreshScope
    public Action negociateSpnego() {
        final SpnegoProperties spnegoProperties = casProperties.getAuthn().getSpnego();
        final List<String> supportedBrowsers = Stream.of(spnegoProperties.getSupportedBrowsers().split(",")).collect(Collectors.toList());
        return new SpnegoNegociateCredentialsAction(supportedBrowsers, spnegoProperties.isNtlm(), spnegoProperties.isMixedModeAuthentication());
    }

    @Bean
    @RefreshScope
    public Action baseSpnegoClientAction() {
        final SpnegoProperties spnegoProperties = casProperties.getAuthn().getSpnego();
        return new BaseSpnegoKnownClientSystemsFilterAction(spnegoProperties.getIpsToCheckPattern(),
                spnegoProperties.getAlternativeRemoteHostAttribute(),
                spnegoProperties.getDnsTimeout());
    }

    @Bean
    @RefreshScope
    public Action hostnameSpnegoClientAction() {
        final SpnegoProperties spnegoProperties = casProperties.getAuthn().getSpnego();
        return new HostNameSpnegoKnownClientSystemsFilterAction(spnegoProperties.getIpsToCheckPattern(),
                spnegoProperties.getAlternativeRemoteHostAttribute(),
                spnegoProperties.getDnsTimeout(), spnegoProperties.getHostNamePatternString());
    }

    @Lazy
    @Bean
    @RefreshScope
    public Action ldapSpnegoClientAction() {
        final SpnegoProperties spnegoProperties = casProperties.getAuthn().getSpnego();
        final ConnectionFactory connectionFactory = LdapUtils.newLdaptivePooledConnectionFactory(spnegoProperties.getLdap());
        final SearchFilter filter = LdapUtils.newLdaptiveSearchFilter(spnegoProperties.getLdap().getSearchFilter(),
                "host", new ArrayList<>(0));

        final SearchRequest searchRequest = LdapUtils.newLdaptiveSearchRequest(spnegoProperties.getLdap().getBaseDn(), filter);
        return new LdapSpnegoKnownClientSystemsFilterAction(spnegoProperties.getIpsToCheckPattern(), 
                spnegoProperties.getAlternativeRemoteHostAttribute(),
                spnegoProperties.getDnsTimeout(), 
                connectionFactory, 
                searchRequest, 
                spnegoProperties.getSpnegoAttributeName());
    }
}
