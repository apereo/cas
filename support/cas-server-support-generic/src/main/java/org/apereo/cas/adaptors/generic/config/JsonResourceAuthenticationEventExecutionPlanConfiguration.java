package org.apereo.cas.adaptors.generic.config;

import org.apereo.cas.adaptors.generic.JsonResourceAuthenticationHandler;
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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * This is {@link JsonResourceAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@Configuration(value = "jsonResourceAuthenticationEventExecutionPlanConfiguration", proxyBeanMethods = false)
public class JsonResourceAuthenticationEventExecutionPlanConfiguration {

    @ConditionalOnMissingBean(name = "jsonPrincipalFactory")
    @Bean
    public PrincipalFactory jsonPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Bean
    @Autowired
    public AuthenticationHandler jsonResourceAuthenticationHandler(final CasConfigurationProperties casProperties, final ConfigurableApplicationContext applicationContext,
                                                                   @Qualifier("jsonPrincipalFactory")
                                                                   final PrincipalFactory jsonPrincipalFactory,
                                                                   @Qualifier(ServicesManager.BEAN_NAME)
                                                                   final ServicesManager servicesManager) {
        val jsonProps = casProperties.getAuthn().getJson();
        val h = new JsonResourceAuthenticationHandler(jsonProps.getName(), servicesManager, jsonPrincipalFactory, null, jsonProps.getLocation());
        h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(jsonProps.getPasswordEncoder(), applicationContext));
        if (jsonProps.getPasswordPolicy().isEnabled()) {
            h.setPasswordPolicyConfiguration(new PasswordPolicyContext(jsonProps.getPasswordPolicy()));
        }
        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(jsonProps.getPrincipalTransformation()));
        h.setState(jsonProps.getState());
        return h;
    }

    @ConditionalOnMissingBean(name = "jsonResourceAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @Autowired
    public AuthenticationEventExecutionPlanConfigurer jsonResourceAuthenticationEventExecutionPlanConfigurer(final CasConfigurationProperties casProperties,
                                                                                                             @Qualifier("jsonResourceAuthenticationHandler")
                                                                                                             final AuthenticationHandler jsonResourceAuthenticationHandler,
                                                                                                             @Qualifier("defaultPrincipalResolver")
                                                                                                             final PrincipalResolver defaultPrincipalResolver) {
        return plan -> {
            val file = casProperties.getAuthn().getJson().getLocation();
            if (file != null) {
                LOGGER.debug("Added JSON resource authentication handler for the target file [{}]", file.getFilename());
                plan.registerAuthenticationHandlerWithPrincipalResolver(jsonResourceAuthenticationHandler, defaultPrincipalResolver);
            }
        };
    }
}
