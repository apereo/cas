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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

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
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .findAndRegisterModules()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
        .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, false)
        .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("defaultPrincipalResolver")
    private ObjectProvider<PrincipalResolver> defaultPrincipalResolver;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Bean
    @RefreshScope
    @SneakyThrows
    public RestTemplate restAuthenticationTemplate() {
        val rest = casProperties.getAuthn().getRest();
        val template = new RestTemplate();
        template.getMessageConverters().stream()
            .filter(c -> c instanceof MappingJackson2HttpMessageConverter)
            .map(MappingJackson2HttpMessageConverter.class::cast)
            .findFirst()
            .ifPresent(converter -> {
                converter.setPrettyPrint(true);
                converter.setDefaultCharset(Charset.forName(rest.getCharset()));
                converter.setObjectMapper(MAPPER);
            });
        return template;
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
        val rest = casProperties.getAuthn().getRest();
        return new RestAuthenticationApi(restAuthenticationTemplate(),
            rest.getUri(),
            rest.getCharset());
    }

    @Bean
    public AuthenticationHandler restAuthenticationHandler() {
        val rest = casProperties.getAuthn().getRest();
        val r = new RestAuthenticationHandler(rest.getName(), restAuthenticationApi(),
            servicesManager.getObject(), restAuthenticationPrincipalFactory());
        r.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(rest.getPasswordEncoder(), applicationContext));
        return r;
    }

    @ConditionalOnMissingBean(name = "casRestAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer casRestAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            if (StringUtils.isNotBlank(casProperties.getAuthn().getRest().getUri())) {
                plan.registerAuthenticationHandlerWithPrincipalResolver(restAuthenticationHandler(), defaultPrincipalResolver.getObject());
            }
        };
    }
}
