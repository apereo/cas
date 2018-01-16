package org.apereo.cas.adaptors.generic.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.generic.JsonResourceAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.authentication.support.password.PasswordPolicyConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.generic.JsonResourceAuthenticationProperties;
import org.apereo.cas.services.ServicesManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

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
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @ConditionalOnMissingBean(name = "jsonPrincipalFactory")
    @Bean
    public PrincipalFactory jsonPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @RefreshScope
    @Bean
    public AuthenticationHandler jsonResourceAuthenticationHandler() {
        final JsonResourceAuthenticationProperties jsonProps = casProperties.getAuthn().getJson();
        final JsonResourceAuthenticationHandler h =
            new JsonResourceAuthenticationHandler(jsonProps.getName(), servicesManager, jsonPrincipalFactory(),
                null, jsonProps.getLocation());
        h.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(jsonProps.getPasswordEncoder()));
        if (jsonProps.getPasswordPolicy().isEnabled()) {
            h.setPasswordPolicyConfiguration(new PasswordPolicyConfiguration(jsonProps.getPasswordPolicy()));
        }
        h.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(jsonProps.getPrincipalTransformation()));
        return h;
    }

    @ConditionalOnMissingBean(name = "jsonResourceAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer jsonResourceAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            final Resource file = casProperties.getAuthn().getJson().getLocation();
            if (file != null) {
                LOGGER.debug("Added JSON resource authentication handler for the target file [{}]", file.getDescription());
                plan.registerAuthenticationHandlerWithPrincipalResolver(jsonResourceAuthenticationHandler(), personDirectoryPrincipalResolver);
            }
        };
    }
}
