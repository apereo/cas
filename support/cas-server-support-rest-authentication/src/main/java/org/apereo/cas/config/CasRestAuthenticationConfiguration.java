package org.apereo.cas.config;

import org.apereo.cas.adaptors.rest.RestAuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
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
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "casRestAuthenticationConfiguration", proxyBeanMethods = false)
public class CasRestAuthenticationConfiguration {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder().defaultTypingEnabled(false).singleValueAsArray(true).build().toObjectMapper();

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Autowired
    public RestTemplate restAuthenticationTemplate(final CasConfigurationProperties casProperties) {
        val rest = casProperties.getAuthn().getRest();
        val template = new RestTemplate();
        template.getMessageConverters()
            .stream()
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
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PrincipalFactory restAuthenticationPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @ConditionalOnMissingBean(name = "restAuthenticationHandler")
    @Autowired
    public AuthenticationHandler restAuthenticationHandler(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("restAuthenticationPrincipalFactory")
        final PrincipalFactory restAuthenticationPrincipalFactory,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        val rest = casProperties.getAuthn().getRest();
        val r = new RestAuthenticationHandler(servicesManager, restAuthenticationPrincipalFactory, rest);
        r.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(rest.getPasswordEncoder(), applicationContext));
        r.setState(rest.getState());
        return r;
    }

    @ConditionalOnMissingBean(name = "casRestAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @Autowired
    public AuthenticationEventExecutionPlanConfigurer casRestAuthenticationEventExecutionPlanConfigurer(
        final CasConfigurationProperties casProperties,
        @Qualifier("restAuthenticationHandler")
        final AuthenticationHandler restAuthenticationHandler,
        @Qualifier("defaultPrincipalResolver")
        final PrincipalResolver defaultPrincipalResolver) {
        return plan -> {
            if (StringUtils.isNotBlank(casProperties.getAuthn().getRest().getUri())) {
                plan.registerAuthenticationHandlerWithPrincipalResolver(restAuthenticationHandler, defaultPrincipalResolver);
            }
        };
    }
}
