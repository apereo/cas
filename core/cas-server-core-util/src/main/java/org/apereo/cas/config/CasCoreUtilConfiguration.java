package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.feature.CasRuntimeModuleLoader;
import org.apereo.cas.util.feature.DefaultCasRuntimeModuleLoader;
import org.apereo.cas.util.scripting.ExecutableCompiledGroovyScript;
import org.apereo.cas.util.scripting.GroovyScriptResourceCacheManager;
import org.apereo.cas.util.scripting.ScriptResourceCacheManager;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.Converters;
import org.apereo.cas.util.spring.SpringAwareMessageMessageInterpolator;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.Ordered;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.Assert;
import org.springframework.validation.beanvalidation.BeanValidationPostProcessor;

import javax.validation.MessageInterpolator;
import java.time.ZonedDateTime;

/**
 * This is {@link CasCoreUtilConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casCoreUtilConfiguration", proxyBeanMethods = true)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@EnableScheduling
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreUtilConfiguration implements InitializingBean {
    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ApplicationContextProvider casApplicationContextProvider() {
        return new ApplicationContextProvider();
    }

    @Bean
    public MessageInterpolator messageInterpolator() {
        return new SpringAwareMessageMessageInterpolator();
    }

    @Bean
    public Converter<ZonedDateTime, String> zonedDateTimeToStringConverter() {
        return new Converters.ZonedDateTimeToStringConverter();
    }

    @Bean
    @ConditionalOnMissingBean(name = "casBeanValidationPostProcessor")
    public BeanValidationPostProcessor casBeanValidationPostProcessor() {
        return new BeanValidationPostProcessor();
    }

    @Bean
    @ConditionalOnMissingBean(name = ScriptResourceCacheManager.BEAN_NAME)
    public ScriptResourceCacheManager<String, ExecutableCompiledGroovyScript> scriptResourceCacheManager() {
        return new GroovyScriptResourceCacheManager();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JacksonObjectMapperFactory.builder().build().toObjectMapper();
    }

    @Bean
    public CasRuntimeModuleLoader casRuntimeModuleLoader() {
        return new DefaultCasRuntimeModuleLoader();
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(casApplicationContextProvider(), "Application context cannot be initialized");
        val registry = (ConverterRegistry) DefaultConversionService.getSharedInstance();
        registry.addConverter(zonedDateTimeToStringConverter());
    }
}
