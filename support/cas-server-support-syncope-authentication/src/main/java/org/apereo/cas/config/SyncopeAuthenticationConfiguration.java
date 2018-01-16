package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.syncope.SyncopeAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.syncope.authentication.SyncopeAuthenticationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SyncopeAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Configuration("syncopeAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class SyncopeAuthenticationConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired(required = false)
    @Qualifier("syncopePasswordPolicyConfiguration")
    private PasswordPolicyConfiguration syncopePasswordPolicyConfiguration;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;


    @ConditionalOnMissingBean(name = "syncopePrincipalFactory")
    @Bean
    public PrincipalFactory syncopePrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "syncopeAuthenticationHandler")
    @Bean
    public AuthenticationHandler syncopeAuthenticationHandler() {
        final SyncopeAuthenticationProperties syncope = casProperties.getAuthn().getSyncope();
        final SyncopeAuthenticationHandler h = new SyncopeAuthenticationHandler(syncope.getName(), servicesManager,
            syncopePrincipalFactory(), syncope.getUrl(), syncope.getDomain());

        h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(syncope.getPasswordEncoder()));
        if (syncopePasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(syncopePasswordPolicyConfiguration);
        }
        h.setCredentialSelectionPredicate(CoreAuthenticationUtils.newCredentialSelectionPredicate(syncope.getCredentialCriteria()));
        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(syncope.getPrincipalTransformation()));
        
        return h;
    }

    @ConditionalOnMissingBean(name = "syncopeAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer tokenAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(syncopeAuthenticationHandler(), personDirectoryPrincipalResolver);
    }

}
