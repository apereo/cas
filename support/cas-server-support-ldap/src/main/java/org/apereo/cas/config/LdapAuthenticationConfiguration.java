package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.LdapAuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.DefaultLdapAccountStateHandler;
import org.apereo.cas.authentication.support.OptionalWarningLdapAccountStateHandler;
import org.apereo.cas.authentication.support.RejectResultCodeLdapPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.support.password.DefaultPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.support.password.GroovyPasswordPolicyHandlingStrategy;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthenticationProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapPasswordPolicyProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.apereo.cas.util.LoggingUtils;

import com.google.common.collect.Multimap;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.ldaptive.auth.AuthenticationResponse;
import org.ldaptive.auth.AuthenticationResponseHandler;
import org.ldaptive.auth.Authenticator;
import org.ldaptive.auth.ext.ActiveDirectoryAuthenticationResponseHandler;
import org.ldaptive.auth.ext.EDirectoryAuthenticationResponseHandler;
import org.ldaptive.auth.ext.FreeIPAAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordExpirationAuthenticationResponseHandler;
import org.ldaptive.auth.ext.PasswordPolicyAuthenticationResponseHandler;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.SetFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This is {@link LdapAuthenticationConfiguration} that attempts to create
 * relevant authentication handlers for LDAP.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Configuration("ldapAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class LdapAuthenticationConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;


    @ConditionalOnMissingBean(name = "ldapPrincipalFactory")
    @Bean
    @RefreshScope
    public PrincipalFactory ldapPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    public SetFactoryBean ldapAuthenticationHandlerSetFactoryBean() {
        return LdapUtils.createLdapAuthenticationFactoryBean();
    }

    @Bean
    @SneakyThrows
    @RefreshScope
    public Collection<AuthenticationHandler> ldapAuthenticationHandlers(
            @Qualifier("ldapAuthenticationHandlerSetFactoryBean")
            final SetFactoryBean ldapAuthenticationHandlerSetFactoryBean) {
        val handlers = new HashSet<AuthenticationHandler>();

        casProperties.getAuthn().getLdap()
            .stream()
            .filter(l -> {
                if (l.getType() == null) {
                    LOGGER.warn("Skipping LDAP authentication entry since no type is defined");
                    return false;
                }
                if (StringUtils.isBlank(l.getLdapUrl())) {
                    LOGGER.warn("Skipping LDAP authentication entry since no LDAP url is defined");
                    return false;
                }
                return true;
            })
            .forEach(l -> {
                val handler = LdapUtils.createLdapAuthenticationHandler(l, applicationContext,
                    servicesManager.getObject(), ldapPrincipalFactory());
                handlers.add(handler);
            });
        ldapAuthenticationHandlerSetFactoryBean.getObject().addAll(handlers);
        return handlers;
    }


    @ConditionalOnMissingBean(name = "ldapAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @Autowired
    @RefreshScope
    public AuthenticationEventExecutionPlanConfigurer ldapAuthenticationEventExecutionPlanConfigurer(
            @Qualifier("ldapAuthenticationHandlerSetFactoryBean")
            final SetFactoryBean ldapAuthenticationHandlerSetFactoryBean) {
        return plan -> ldapAuthenticationHandlers(ldapAuthenticationHandlerSetFactoryBean).forEach(handler -> {
            LOGGER.info("Registering LDAP authentication for [{}]", handler.getName());
            plan.registerAuthenticationHandlerWithPrincipalResolver(handler, defaultPrincipalResolver.getObject());
        });
    }
}
