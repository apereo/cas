package org.apereo.cas.adaptors.trusted.config;

import org.apereo.cas.adaptors.trusted.authentication.handler.support.PrincipalBearingCredentialsAuthenticationHandler;
import org.apereo.cas.adaptors.trusted.authentication.principal.PrincipalBearingPrincipalResolver;
import org.apereo.cas.adaptors.trusted.web.flow.PrincipalFromRequestRemoteUserNonInteractiveCredentialsAction;
import org.apereo.cas.adaptors.trusted.web.flow.PrincipalFromRequestUserPrincipalNonInteractiveCredentialsAction;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.principal.resolvers.ChainingPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.EchoingPrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.trusted.TrustedAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

import java.util.Arrays;

/**
 * This is {@link TrustedAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("trustedAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class TrustedAuthenticationConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

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
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;

    @Bean
    @RefreshScope
    public AuthenticationHandler principalBearingCredentialsAuthenticationHandler() {
        final TrustedAuthenticationProperties trusted = casProperties.getAuthn().getTrusted();
        return new PrincipalBearingCredentialsAuthenticationHandler(trusted.getName(), servicesManager, trustedPrincipalFactory());
    }

    @Bean
    @RefreshScope
    public PrincipalResolver trustedPrincipalResolver() {
        final ChainingPrincipalResolver resolver = new ChainingPrincipalResolver();

        final PrincipalBearingPrincipalResolver bearingPrincipalResolver = new PrincipalBearingPrincipalResolver();
        bearingPrincipalResolver.setAttributeRepository(this.attributeRepository);
        bearingPrincipalResolver.setPrincipalAttributeName(casProperties.getAuthn().getTrusted().getPrincipalAttribute());
        bearingPrincipalResolver.setReturnNullIfNoAttributes(casProperties.getAuthn().getTrusted().isReturnNull());
        bearingPrincipalResolver.setPrincipalFactory(trustedPrincipalFactory());
        
        resolver.setChain(Arrays.asList(bearingPrincipalResolver, new EchoingPrincipalResolver()));
        return resolver;
    }

    @ConditionalOnMissingBean(name = "trustedPrincipalFactory")
    @Bean
    public PrincipalFactory trustedPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    @RefreshScope
    public Action principalFromRemoteUserAction() {
        return new PrincipalFromRequestRemoteUserNonInteractiveCredentialsAction(initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy,
                trustedPrincipalFactory());
    }

    @Bean
    @RefreshScope
    public Action principalFromRemoteUserPrincipalAction() {
        return new PrincipalFromRequestUserPrincipalNonInteractiveCredentialsAction(initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy,
                trustedPrincipalFactory());
    }

    /**
     * The type Trusted authentication event execution plan configuration.
     */
    @Configuration("trustedAuthenticationEventExecutionPlanConfiguration")
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public class TrustedAuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {
        @Override
        public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
            plan.registerAuthenticationHandlerWithPrincipalResolver(principalBearingCredentialsAuthenticationHandler(), trustedPrincipalResolver());
        }
    }
}
