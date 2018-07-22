package org.apereo.cas.config;

import org.apereo.cas.adaptors.rest.RestAuthenticationApi;
import org.apereo.cas.adaptors.rest.RestAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * This is {@link CasRestAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Configuration("casRestAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasRestAuthenticationConfiguration {

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @RefreshScope
    @SneakyThrows
    public RestTemplate restAuthenticationTemplate() {
        return new RestTemplate();
    }

    @ConditionalOnMissingBean(name = "restAuthenticationPrincipalFactory")
    @Bean
    @RefreshScope
    public PrincipalFactory restAuthenticationPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @ConditionalOnMissingBean(name = "restAuthenticationApi")
    @Bean
    @RefreshScope
    public RestAuthenticationApi restAuthenticationApi() {
        return new RestAuthenticationApi(restAuthenticationTemplate(), casProperties.getAuthn().getRest().getUri());
    }

    @Bean
    public AuthenticationHandler restAuthenticationHandler() {
        val rest = casProperties.getAuthn().getRest();
        val r = new RestAuthenticationHandler(rest.getName(), restAuthenticationApi(),
            servicesManager, restAuthenticationPrincipalFactory());
        r.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(rest.getPasswordEncoder()));
        return r;
    }

    @ConditionalOnMissingBean(name = "casRestAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer casRestAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            if (StringUtils.isNotBlank(casProperties.getAuthn().getRest().getUri())) {
                plan.registerAuthenticationHandlerWithPrincipalResolver(restAuthenticationHandler(), personDirectoryPrincipalResolver);
            }
        };
    }
}
