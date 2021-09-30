package org.apereo.cas.digest.config.support.authentication;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.digest.DigestAuthenticationHandler;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link DigestAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "digestAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
public class DigestAuthenticationEventExecutionPlanConfiguration {

    @ConditionalOnMissingBean(name = "digestAuthenticationPrincipalFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalFactory digestAuthenticationPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "digestAuthenticationHandler")
    @Autowired
    public AuthenticationHandler digestAuthenticationHandler(final CasConfigurationProperties casProperties,
                                                             @Qualifier("digestAuthenticationPrincipalFactory")
                                                             final PrincipalFactory digestAuthenticationPrincipalFactory,
                                                             @Qualifier(ServicesManager.BEAN_NAME)
                                                             final ServicesManager servicesManager) {
        val digest = casProperties.getAuthn().getDigest();
        return new DigestAuthenticationHandler(digest.getName(), servicesManager, digestAuthenticationPrincipalFactory, digest.getOrder());
    }

    @ConditionalOnMissingBean(name = "digestAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer digestAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("digestAuthenticationHandler")
        final AuthenticationHandler digestAuthenticationHandler,
        @Qualifier("defaultPrincipalResolver")
        final PrincipalResolver defaultPrincipalResolver) {
        return plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(digestAuthenticationHandler, defaultPrincipalResolver);
    }
}
