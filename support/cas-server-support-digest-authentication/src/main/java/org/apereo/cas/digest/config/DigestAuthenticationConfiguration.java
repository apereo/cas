package org.apereo.cas.digest.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.config.support.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.digest.DigestProperties;
import org.apereo.cas.digest.DefaultDigestHashedCredentialRetriever;
import org.apereo.cas.digest.DigestAuthenticationHandler;
import org.apereo.cas.digest.DigestHashedCredentialRetriever;
import org.apereo.cas.digest.web.flow.DigestAuthenticationAction;
import org.apereo.cas.digest.web.flow.DigestAuthenticationWebflowConfigurer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link DigestAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("digestAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class DigestAuthenticationConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Autowired
    @Qualifier("adaptiveAuthenticationPolicy")
    private AdaptiveAuthenticationPolicy adaptiveAuthenticationPolicy;

    @Autowired
    @Qualifier("serviceTicketRequestWebflowEventResolver")
    private CasWebflowEventResolver serviceTicketRequestWebflowEventResolver;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @ConditionalOnMissingBean(name = "digestAuthenticationPrincipalFactory")
    @Bean
    public PrincipalFactory digestAuthenticationPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "digestAuthenticationWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer digestAuthenticationWebflowConfigurer() {
        return new DigestAuthenticationWebflowConfigurer(flowBuilderServices, loginFlowDefinitionRegistry);
    }

    @Autowired
    @RefreshScope
    @Bean
    public DigestAuthenticationAction digestAuthenticationAction(@Qualifier("defaultDigestCredentialRetriever")
                                                                 final DigestHashedCredentialRetriever defaultDigestCredentialRetriever) {
        return new DigestAuthenticationAction(initialAuthenticationAttemptWebflowEventResolver,
                serviceTicketRequestWebflowEventResolver,
                adaptiveAuthenticationPolicy,
                casProperties.getAuthn().getDigest().getRealm(),
                casProperties.getAuthn().getDigest().getAuthenticationMethod(),
                defaultDigestCredentialRetriever);
    }

    @ConditionalOnMissingBean(name = "defaultDigestCredentialRetriever")
    @Bean
    @RefreshScope
    public DigestHashedCredentialRetriever defaultDigestCredentialRetriever() {
        final DigestProperties digest = casProperties.getAuthn().getDigest();
        return new DefaultDigestHashedCredentialRetriever(digest.getUsers());
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler digestAuthenticationHandler() {
        final DigestProperties digest = casProperties.getAuthn().getDigest();
        final DigestAuthenticationHandler r = new DigestAuthenticationHandler();
        r.setPrincipalFactory(digestAuthenticationPrincipalFactory());
        r.setServicesManager(servicesManager);
        r.setName(digest.getName());
        return r;
    }

    @Configuration("digestAuthenticationEventExecutionPlanConfiguration")
    public class DigestAuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {
        @Autowired
        @Qualifier("personDirectoryPrincipalResolver")
        private PrincipalResolver personDirectoryPrincipalResolver;

        @Override
        public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
            plan.registerAuthenticationHandlerWithPrincipalResolver(digestAuthenticationHandler(), personDirectoryPrincipalResolver);
        }
    }
}
