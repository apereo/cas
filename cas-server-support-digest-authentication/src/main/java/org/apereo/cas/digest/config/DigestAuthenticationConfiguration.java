package org.apereo.cas.digest.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.digest.DefaultDigestHashedCredentialRetriever;
import org.apereo.cas.digest.DigestHashedCredentialRetriever;
import org.apereo.cas.digest.web.flow.DigestAuthenticationAction;
import org.apereo.cas.digest.web.flow.DigestAuthenticationWebflowConfigurer;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.CookieGenerator;
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
    @Qualifier("warnCookieGenerator")
    private CookieGenerator warnCookieGenerator;

    @Autowired
    @Qualifier("loginFlowRegistry")
    private FlowDefinitionRegistry loginFlowDefinitionRegistry;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    private FlowBuilderServices flowBuilderServices;

    @Bean
    public PrincipalFactory digestAuthenticationPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    public CasWebflowConfigurer digestAuthenticationWebflowConfigurer() {
        final DigestAuthenticationWebflowConfigurer w = new DigestAuthenticationWebflowConfigurer();
        w.setLoginFlowDefinitionRegistry(loginFlowDefinitionRegistry);
        w.setFlowBuilderServices(flowBuilderServices);
        return w;
    }

    @RefreshScope
    @Bean
    public DigestAuthenticationAction digestAuthenticationAction() {
        final DigestAuthenticationAction w = new DigestAuthenticationAction();
        w.setWarnCookieGenerator(warnCookieGenerator);
        w.setAuthenticationSystemSupport(authenticationSystemSupport);
        w.setCentralAuthenticationService(centralAuthenticationService);
        w.setPrincipalFactory(digestAuthenticationPrincipalFactory());

        w.setRealm(casProperties.getAuthn().getDigest().getRealm());
        w.setAuthenticationMethod(casProperties.getAuthn().getDigest().getAuthenticationMethod());
        w.setCredentialRetriever(defaultCredentialRetriever());
        return w;
    }

    @ConditionalOnMissingBean(name = "defaultCredentialRetriever")
    @Bean
    @RefreshScope
    public DigestHashedCredentialRetriever defaultCredentialRetriever() {
        final DefaultDigestHashedCredentialRetriever r = new DefaultDigestHashedCredentialRetriever();
        casProperties.getAuthn().getDigest().getUsers().forEach((k, v) ->
                r.getStore().put(k, casProperties.getAuthn().getDigest().getRealm(), v));
        return r;
    }

}
