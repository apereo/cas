package org.apereo.cas.config.support.authentication;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.AsciiArtUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link AcceptUsersAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration(value = "acceptUsersAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class AcceptUsersAuthenticationEventExecutionPlanConfiguration {
    @ConditionalOnMissingBean(name = "acceptUsersAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public AuthenticationEventExecutionPlanConfigurer acceptUsersAuthenticationEventExecutionPlanConfigurer(
        @Qualifier("acceptUsersAuthenticationHandler")
        final AuthenticationHandler acceptUsersAuthenticationHandler,
        @Qualifier("defaultPrincipalResolver")
        final PrincipalResolver defaultPrincipalResolver,
        final CasConfigurationProperties casProperties) {
        return plan -> {
            val accept = casProperties.getAuthn().getAccept();
            if (accept.isEnabled() && StringUtils.isNotBlank(accept.getUsers())) {
                plan.registerAuthenticationHandlerWithPrincipalResolver(acceptUsersAuthenticationHandler, defaultPrincipalResolver);
            }
        };
    }

    @Bean
    @Autowired
    public InitializingBean acceptUsersAuthenticationInitializingBean(final CasConfigurationProperties casProperties) {
        return () -> {
            val accept = casProperties.getAuthn().getAccept();
            if (accept.isEnabled() && StringUtils.isNotBlank(accept.getUsers())) {
                val header =
                    "\nCAS is configured to accept a static list of credentials for authentication. "
                        + "While this is generally useful for demo purposes, it is STRONGLY recommended "
                        + "that you DISABLE this authentication method by setting 'cas.authn.accept.enabled=false' "
                        + "and switch to a mode that is more suitable for production.";
                AsciiArtUtils.printAsciiArtWarning(LOGGER, header);
            }
        };
    }
}
