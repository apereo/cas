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
 * This is {@link JsonResourceAuthenticationEventExecutionPlanConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration("jsonResourceAuthenticationEventExecutionPlanConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class JsonResourceAuthenticationEventExecutionPlanConfiguration {
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

    @ConditionalOnMissingBean(name = "jsonPrincipalFactory")
    @Bean
    public PrincipalFactory jsonPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler jsonResourceAuthenticationHandler() {
        val jsonProps = casProperties.getAuthn().getJson();
        val h = new JsonResourceAuthenticationHandler(jsonProps.getName(), servicesManager.getObject(), jsonPrincipalFactory(),
            null, jsonProps.getLocation());
        h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(jsonProps.getPasswordEncoder(), applicationContext));
        if (jsonProps.getPasswordPolicy().isEnabled()) {
            h.setPasswordPolicyConfiguration(new PasswordPolicyContext(jsonProps.getPasswordPolicy()));
        }
        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(jsonProps.getPrincipalTransformation()));
        return h;
    }

    @ConditionalOnMissingBean(name = "jsonResourceAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer jsonResourceAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            val file = casProperties.getAuthn().getJson().getLocation();
            if (file != null) {
                LOGGER.debug("Added JSON resource authentication handler for the target file [{}]", file.getFilename());
                plan.registerAuthenticationHandlerWithPrincipalResolver(jsonResourceAuthenticationHandler(), defaultPrincipalResolver.getObject());
            }
        };
    }
}
