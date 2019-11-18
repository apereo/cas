package org.apereo.cas.adaptors.generic.config;

import org.apereo.cas.adaptors.generic.FileAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link FileAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration("fileAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class FileAuthenticationEventExecutionPlanConfiguration {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    @ConditionalOnMissingBean(name = "filePrincipalFactory")
    @Bean
    public PrincipalFactory filePrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }


    @RefreshScope
    @Bean
    public AuthenticationHandler fileAuthenticationHandler() {
        val fileProperties = casProperties.getAuthn().getFile();
        val h = new FileAuthenticationHandler(fileProperties.getName(), servicesManager.getObject(), filePrincipalFactory(),
            fileProperties.getFilename(), fileProperties.getSeparator());

        h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(fileProperties.getPasswordEncoder(), applicationContext));
        h.setPasswordPolicyConfiguration(filePasswordPolicyConfiguration());
        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(fileProperties.getPrincipalTransformation()));

        return h;
    }

    @ConditionalOnMissingBean(name = "fileAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer fileAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            val file = casProperties.getAuthn().getFile().getFilename();
            if (file != null) {
                LOGGER.debug("Added file-based authentication handler for the target file [{}]", file.getDescription());
                plan.registerAuthenticationHandlerWithPrincipalResolver(fileAuthenticationHandler(), defaultPrincipalResolver.getObject());
            }
        };
    }

    @ConditionalOnMissingBean(name = "filePasswordPolicyConfiguration")
    @Bean
    public PasswordPolicyContext filePasswordPolicyConfiguration() {
        return new PasswordPolicyContext();
    }
}
