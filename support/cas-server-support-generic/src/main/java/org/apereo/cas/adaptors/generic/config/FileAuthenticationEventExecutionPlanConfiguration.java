package org.apereo.cas.adaptors.generic.config;

import org.apereo.cas.adaptors.generic.FileAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordPolicyConfiguration;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.generic.FileAuthenticationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link FileAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("fileAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class FileAuthenticationEventExecutionPlanConfiguration implements AuthenticationEventExecutionPlanConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileAuthenticationEventExecutionPlanConfiguration.class);

    @Autowired(required = false)
    @Qualifier("filePasswordPolicyConfiguration")
    private PasswordPolicyConfiguration filePasswordPolicyConfiguration;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;
    
    @Autowired
    private CasConfigurationProperties casProperties;
    
    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @ConditionalOnMissingBean(name = "filePrincipalFactory")
    @Bean
    public PrincipalFactory filePrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    
    @RefreshScope
    @Bean
    public AuthenticationHandler fileAuthenticationHandler() {
        final FileAuthenticationProperties fileProperties = casProperties.getAuthn().getFile();
        final FileAuthenticationHandler h = new FileAuthenticationHandler(fileProperties.getName(), servicesManager, filePrincipalFactory(),
                fileProperties.getFilename(), fileProperties.getSeparator());

        h.setPasswordEncoder(Beans.newPasswordEncoder(fileProperties.getPasswordEncoder()));
        if (filePasswordPolicyConfiguration != null) {
            h.setPasswordPolicyConfiguration(filePasswordPolicyConfiguration);
        }
        h.setPrincipalNameTransformer(Beans.newPrincipalNameTransformer(fileProperties.getPrincipalTransformation()));

        return h;
    }
    
    @Override
    public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
        if (casProperties.getAuthn().getFile().getFilename() != null) {
            LOGGER.debug("Added file-based authentication handler");
            plan.registerAuthenticationHandlerWithPrincipalResolver(fileAuthenticationHandler(), personDirectoryPrincipalResolver);
        }
    }
    
    
}
