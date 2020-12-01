package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.services.ServicesManager;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.SetFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This is {@link QrConfiguration}.
 *
 * @author Ben Winston
 * @since 6.2.0
 */
@Configuration("qrConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class QrConfiguration {

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

	@Bean
	public SetFactoryBean qrAuthenticationHandlerSetFactoryBean() {
        val bean = new SetFactoryBean() {
            @Override
            protected void destroyInstance(final Set set) {
                set.forEach(Unchecked.consumer(handler ->
                    ((DisposableBean) handler).destroy()
                ));
            }
        	};
        	bean.setSourceSet(new HashSet<>());
        	return bean;
    	}

	@ConditionalOnMissingBean(name = "qrAuthenticationEventExecutionPlanConfigurer")
	@Bean
	@Autowired
	@RefreshScope
	public AuthenticationEventExecutionPlanConfigurer qrAuthenticationEventExecutionPlanConfigurer(
            @Qualifier("qrAuthenticationHandlerSetFactoryBean")
            final SetFactoryBean qrAuthenticationHandlerSetFactoryBean) {

		LOGGER.warn("~!~!~ in the Executor");

		return null;
		/*
		return plan -> ldapAuthenticationHandlers(ldapAuthenticationHandlerSetFactoryBean).forEach(handler -> {
            LOGGER.info("Registering LDAP authentication for [{}]", handler.getName());
            plan.registerAuthenticationHandlerWithPrincipalResolver(handler, defaultPrincipalResolver.getObject());
        });
	*/
    }

    @Bean
    @SneakyThrows
    @RefreshScope
    public Collection<AuthenticationHandler> qrAuthenticationHandlers(
            @Qualifier("qrAuthenticationHandlerSetFactoryBean")
            final SetFactoryBean qrAuthenticationHandlerSetFactoryBean) {
        val handlers = new HashSet<AuthenticationHandler>();

	LOGGER.warn("~?~?~ in the Authentication Handlers bit");

	return handlers;
    }

}
