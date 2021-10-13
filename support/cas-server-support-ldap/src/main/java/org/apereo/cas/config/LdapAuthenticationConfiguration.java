package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.spring.BeanContainer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.SetFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.HashSet;

/**
 * This is {@link LdapAuthenticationConfiguration} that attempts to create
 * relevant authentication handlers for LDAP.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@Configuration(value = "ldapAuthenticationConfiguration", proxyBeanMethods = false)
public class LdapAuthenticationConfiguration {

    @ConditionalOnMissingBean(name = "ldapPrincipalFactory")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalFactory ldapPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    public SetFactoryBean ldapAuthenticationHandlerSetFactoryBean() {
        return LdapUtils.createLdapAuthenticationFactoryBean();
    }

    @Configuration(value = "LdapAuthenticationPlanConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class LdapAuthenticationPlanConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public BeanContainer<AuthenticationHandler> ldapAuthenticationHandlers(
            @Qualifier("ldapAuthenticationHandlerSetFactoryBean")
            final SetFactoryBean ldapAuthenticationHandlerSetFactoryBean,
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("ldapPrincipalFactory")
            final PrincipalFactory ldapPrincipalFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) throws Exception {
            val handlers = new HashSet<AuthenticationHandler>();
            casProperties.getAuthn().getLdap().stream().filter(l -> {
                if (l.getType() == null || StringUtils.isBlank(l.getLdapUrl())) {
                    LOGGER.warn("Skipping LDAP authentication entry since no type or LDAP url is defined");
                    return false;
                }
                return true;
            }).forEach(l -> {
                val handler = LdapUtils.createLdapAuthenticationHandler(l, applicationContext, servicesManager, ldapPrincipalFactory);
                handler.setState(l.getState());
                handlers.add(handler);
            });
            ldapAuthenticationHandlerSetFactoryBean.getObject().addAll(handlers);
            return BeanContainer.of(handlers);
        }

        @ConditionalOnMissingBean(name = "ldapAuthenticationEventExecutionPlanConfigurer")
        @Bean
        @Autowired
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AuthenticationEventExecutionPlanConfigurer ldapAuthenticationEventExecutionPlanConfigurer(
            @Qualifier("ldapAuthenticationHandlers")
            final BeanContainer<AuthenticationHandler> ldapAuthenticationHandlers,
            @Qualifier("defaultPrincipalResolver")
            final PrincipalResolver defaultPrincipalResolver) throws Exception {
            return plan -> ldapAuthenticationHandlers.toList().forEach(handler -> {
                LOGGER.info("Registering LDAP authentication for [{}]", handler.getName());
                plan.registerAuthenticationHandlerWithPrincipalResolver(handler, defaultPrincipalResolver);
            });
        }
    }
}
