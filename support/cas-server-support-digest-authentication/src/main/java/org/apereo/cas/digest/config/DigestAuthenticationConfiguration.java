package org.apereo.cas.digest.config;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.adaptive.AdaptiveAuthenticationPolicy;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.digest.DefaultDigestHashedCredentialRetriever;
import org.apereo.cas.digest.DigestAuthenticationHandler;
import org.apereo.cas.digest.DigestHashedCredentialRetriever;
import org.apereo.cas.digest.web.flow.DigestAuthenticationAction;
import org.apereo.cas.digest.web.flow.DigestAuthenticationWebflowConfigurer;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
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

import javax.annotation.PostConstruct;
import java.util.Map;

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
    @Qualifier("authenticationHandlersResolvers")
    private Map authenticationHandlersResolvers;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

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
    private CasWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @Bean
    public PrincipalFactory digestAuthenticationPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "digestAuthenticationWebflowConfigurer")
    @Bean
    public CasWebflowConfigurer digestAuthenticationWebflowConfigurer() {
        final DigestAuthenticationWebflowConfigurer w = new DigestAuthenticationWebflowConfigurer();
        w.setLoginFlowDefinitionRegistry(loginFlowDefinitionRegistry);
        w.setFlowBuilderServices(flowBuilderServices);
        return w;
    }

    @Autowired
    @RefreshScope
    @Bean
    public DigestAuthenticationAction digestAuthenticationAction(
            @Qualifier("defaultDigestCredentialRetriever")
            final DigestHashedCredentialRetriever defaultDigestCredentialRetriever) {
        final DigestAuthenticationAction w = new DigestAuthenticationAction();
        w.setRealm(casProperties.getAuthn().getDigest().getRealm());
        w.setAuthenticationMethod(casProperties.getAuthn().getDigest().getAuthenticationMethod());
        w.setCredentialRetriever(defaultDigestCredentialRetriever);

        w.setAdaptiveAuthenticationPolicy(adaptiveAuthenticationPolicy);
        w.setInitialAuthenticationAttemptWebflowEventResolver(initialAuthenticationAttemptWebflowEventResolver);
        w.setServiceTicketRequestWebflowEventResolver(serviceTicketRequestWebflowEventResolver);

        return w;
    }

    @ConditionalOnMissingBean(name = "defaultDigestCredentialRetriever")
    @Bean
    @RefreshScope
    public DigestHashedCredentialRetriever defaultDigestCredentialRetriever() {
        final DefaultDigestHashedCredentialRetriever r = new DefaultDigestHashedCredentialRetriever();
        casProperties.getAuthn().getDigest().getUsers().forEach((k, v) ->
                r.getStore().put(k, casProperties.getAuthn().getDigest().getRealm(), v));
        return r;
    }

    @Bean
    @RefreshScope
    public AuthenticationHandler digestAuthenticationHandler() {
        final DigestAuthenticationHandler r = new DigestAuthenticationHandler();
        r.setPrincipalFactory(digestAuthenticationPrincipalFactory());
        r.setServicesManager(servicesManager);
        return r;
    }

    @PostConstruct
    public void initializeAuthenticationHandler() {
        this.authenticationHandlersResolvers.put(digestAuthenticationHandler(), personDirectoryPrincipalResolver);
    }

}
