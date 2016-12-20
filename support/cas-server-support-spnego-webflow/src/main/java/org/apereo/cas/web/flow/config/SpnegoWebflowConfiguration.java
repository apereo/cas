package org.apereo.cas.web.flow.config;

import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.spnego.SpnegoProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.SpengoWebflowConfigurer;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.StringUtils;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;

import java.util.Arrays;
import java.util.Collections;

/**
 * This is {@link SpnegoWebflowConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("spnegoWebflowConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class SpnegoWebflowConfiguration {

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
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "spnegoWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer spnegoWebflowConfigurer() {
        return new SpengoWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry);
    }

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
        final SpnegoNegociateCredentialsAction a = new SpnegoNegociateCredentialsAction();
        a.setMixedModeAuthentication(spnegoProperties.isMixedModeAuthentication());
        a.setNtlm(spnegoProperties.isNtlm());
        final String[] browsers = StringUtils.commaDelimitedListToStringArray(spnegoProperties.getSupportedBrowsers());
        a.setSupportedBrowsers(Arrays.asList(browsers));
        return a;
    }

    @Bean
    @RefreshScope
    public Action baseSpnegoClientAction() {
        final SpnegoProperties spnegoProperties = casProperties.getAuthn().getSpnego();
        final BaseSpnegoKnownClientSystemsFilterAction a =
                new BaseSpnegoKnownClientSystemsFilterAction();

        a.setIpsToCheckPattern(spnegoProperties.getIpsToCheckPattern());
        a.setAlternativeRemoteHostAttribute(spnegoProperties.getAlternativeRemoteHostAttribute());
        a.setTimeout(spnegoProperties.getDnsTimeout());
        return a;
    }

    @Bean
    @RefreshScope
    public Action hostnameSpnegoClientAction() {
        final SpnegoProperties spnegoProperties = casProperties.getAuthn().getSpnego();
        final HostNameSpnegoKnownClientSystemsFilterAction a =
                new HostNameSpnegoKnownClientSystemsFilterAction(spnegoProperties.getHostNamePatternString());
        a.setIpsToCheckPattern(spnegoProperties.getIpsToCheckPattern());
        a.setAlternativeRemoteHostAttribute(spnegoProperties.getAlternativeRemoteHostAttribute());
        a.setTimeout(spnegoProperties.getDnsTimeout());
        return a;
    }

    @Lazy
    @Bean
    @RefreshScope
    public Action ldapSpnegoClientAction() {
        final SpnegoProperties spnegoProperties = casProperties.getAuthn().getSpnego();
        final ConnectionFactory connectionFactory = Beans.newPooledConnectionFactory(spnegoProperties.getLdap());
        final SearchFilter filter = Beans.newSearchFilter(spnegoProperties.getLdap().getSearchFilter(),
                "host", Collections.emptyList());

        final SearchRequest searchRequest = Beans.newSearchRequest(
                spnegoProperties.getLdap().getBaseDn(), filter);

        final LdapSpnegoKnownClientSystemsFilterAction l =
                new LdapSpnegoKnownClientSystemsFilterAction(connectionFactory,
                        searchRequest, spnegoProperties.getSpnegoAttributeName());

        l.setIpsToCheckPattern(spnegoProperties.getIpsToCheckPattern());
        l.setAlternativeRemoteHostAttribute(spnegoProperties.getAlternativeRemoteHostAttribute());
        l.setTimeout(spnegoProperties.getDnsTimeout());
        return l;
    }
}
