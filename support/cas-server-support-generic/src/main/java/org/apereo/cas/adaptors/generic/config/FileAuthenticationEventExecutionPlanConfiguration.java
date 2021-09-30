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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link FileAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration(value = "fileAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class FileAuthenticationEventExecutionPlanConfiguration {
    @ConditionalOnMissingBean(name = "filePrincipalFactory")
    @Bean
    public PrincipalFactory filePrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public AuthenticationHandler fileAuthenticationHandler(
        @Qualifier("filePasswordPolicyConfiguration")
        final PasswordPolicyContext filePasswordPolicyConfiguration,
        @Qualifier("filePrincipalFactory")
        final PrincipalFactory filePrincipalFactory,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext) {
        val fileProperties = casProperties.getAuthn().getFile();
        val h = new FileAuthenticationHandler(fileProperties.getName(), servicesManager, filePrincipalFactory,
            fileProperties.getFilename(), fileProperties.getSeparator());
        h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(fileProperties.getPasswordEncoder(), applicationContext));
        h.setPasswordPolicyConfiguration(filePasswordPolicyConfiguration);
        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(fileProperties.getPrincipalTransformation()));

        return h;
    }

    @ConditionalOnMissingBean(name = "fileAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @ConditionalOnProperty(name = "cas.authn.file.filename")
    @Autowired
    public AuthenticationEventExecutionPlanConfigurer fileAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("fileAuthenticationHandler")
        final AuthenticationHandler fileAuthenticationHandler,
        final CasConfigurationProperties casProperties,
        @Qualifier("defaultPrincipalResolver")
        final PrincipalResolver defaultPrincipalResolver) {
        return plan -> {
            val file = casProperties.getAuthn().getFile().getFilename();
            LOGGER.debug("Added file-based authentication handler for the target file [{}]", file.getDescription());
            plan.registerAuthenticationHandlerWithPrincipalResolver(fileAuthenticationHandler, defaultPrincipalResolver);
        };
    }

    @ConditionalOnMissingBean(name = "filePasswordPolicyConfiguration")
    @Bean
    public PasswordPolicyContext filePasswordPolicyConfiguration() {
        return new PasswordPolicyContext();
    }
}
