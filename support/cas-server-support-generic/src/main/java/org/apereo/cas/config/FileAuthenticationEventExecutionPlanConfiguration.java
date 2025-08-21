package org.apereo.cas.config;

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
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication, module = "generic")
@Configuration(value = "FileAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
class FileAuthenticationEventExecutionPlanConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.file.filename").exists();

    @ConditionalOnMissingBean(name = "filePrincipalFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalFactory filePrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    public AuthenticationHandler fileAuthenticationHandler(
        @Qualifier("filePasswordPolicyConfiguration")
        final PasswordPolicyContext filePasswordPolicyConfiguration,
        @Qualifier("filePrincipalFactory")
        final PrincipalFactory filePrincipalFactory,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext) {
        return BeanSupplier.of(AuthenticationHandler.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val fileProperties = casProperties.getAuthn().getFile();
                val h = new FileAuthenticationHandler(fileProperties.getName(), filePrincipalFactory,
                    fileProperties.getFilename(), fileProperties.getSeparator());
                h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(fileProperties.getPasswordEncoder(), applicationContext));
                h.setPasswordPolicyConfiguration(filePasswordPolicyConfiguration);
                h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(fileProperties.getPrincipalTransformation()));
                return h;
            })
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "fileAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationEventExecutionPlanConfigurer fileAuthenticationEventExecutionPlanConfigurer(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("fileAuthenticationHandler")
        final AuthenticationHandler fileAuthenticationHandler,
        final CasConfigurationProperties casProperties,
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
        final PrincipalResolver defaultPrincipalResolver) {
        return BeanSupplier.of(AuthenticationEventExecutionPlanConfigurer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> plan -> {
                val file = casProperties.getAuthn().getFile().getFilename();
                LOGGER.debug("Added file-based authentication handler for the target file [{}]", file.getDescription());
                plan.registerAuthenticationHandlerWithPrincipalResolver(fileAuthenticationHandler, defaultPrincipalResolver);
            })
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "filePasswordPolicyConfiguration")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PasswordPolicyContext filePasswordPolicyConfiguration() {
        return new PasswordPolicyContext();
    }
}
