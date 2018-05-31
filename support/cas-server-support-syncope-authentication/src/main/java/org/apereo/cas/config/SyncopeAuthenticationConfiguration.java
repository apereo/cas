package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.syncope.authentication.SyncopeAuthenticationHandler;
import org.springframework.beans.factory.BeanCreationException;
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

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @ConditionalOnMissingBean(name = "syncopePrincipalFactory")
    @Bean
    public PrincipalFactory syncopePrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "syncopeAuthenticationHandler")
    @Bean
    public AuthenticationHandler syncopeAuthenticationHandler() {
        final var syncope = casProperties.getAuthn().getSyncope();
        if (StringUtils.isBlank(syncope.getUrl())) {
            throw new BeanCreationException("Syncope URL must be defined");
        }
        final var h = new SyncopeAuthenticationHandler(syncope.getName(), servicesManager,
            syncopePrincipalFactory(), syncope.getUrl(), syncope.getDomain());

        h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(syncope.getPasswordEncoder()));
        h.setPasswordPolicyConfiguration(syncopePasswordPolicyConfiguration());
        h.setCredentialSelectionPredicate(CoreAuthenticationUtils.newCredentialSelectionPredicate(syncope.getCredentialCriteria()));
        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(syncope.getPrincipalTransformation()));

        return h;
    }

    @ConditionalOnMissingBean(name = "syncopeAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer syncopeAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(syncopeAuthenticationHandler(), personDirectoryPrincipalResolver);
    }

    @ConditionalOnMissingBean(name = "syncopePasswordPolicyConfiguration")
    @Bean
    public PasswordPolicyConfiguration syncopePasswordPolicyConfiguration() {
        return new PasswordPolicyConfiguration();
    }
}
