package org.apereo.cas.config;

import org.apereo.cas.CasEmbeddedValueResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.SchedulingUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledGroovyScript;
import org.apereo.cas.util.scripting.GroovyScriptResourceCacheManager;
import org.apereo.cas.util.scripting.ScriptResourceCacheManager;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.Converters;
import org.apereo.cas.util.spring.SpringAwareMessageMessageInterpolator;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.val;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.context.annotation.Scope;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.StringValueResolver;
import org.springframework.validation.beanvalidation.BeanValidationPostProcessor;

import javax.validation.MessageInterpolator;
import java.time.ZonedDateTime;

/**
 * This is {@link CasCoreUtilConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casCoreUtilConfiguration")
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@EnableScheduling
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasCoreUtilConfiguration implements InitializingBean {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

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
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public StringValueResolver durationCapableStringValueResolver() {
        return SchedulingUtils.prepScheduledAnnotationBeanPostProcessor(applicationContext);
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

    /**
     * It's important to invoke the {@link #casApplicationContextProvider()}
     * method here forcefully and not rely on the {@link #applicationContext}.
     * Certain tests in the CAS context require access to the application context
     * from the {@link #casApplicationContextProvider()}.
     */
    @Override
    @SuppressFBWarnings("NIR_NEEDLESS_INSTANCE_RETRIEVAL")
    public void afterPropertiesSet() {
        val appContext = casApplicationContextProvider().getConfigurableApplicationContext();
        val conversionService = new DefaultFormattingConversionService(true);
        conversionService.setEmbeddedValueResolver(new CasEmbeddedValueResolver(appContext));
        appContext.getEnvironment().setConversionService(conversionService);
        if (appContext.getParent() != null) {
            var env = (ConfigurableEnvironment) appContext.getParent().getEnvironment();
            env.setConversionService(conversionService);
        }
        val registry = (ConverterRegistry) DefaultConversionService.getSharedInstance();
        registry.addConverter(zonedDateTimeToStringConverter());
    }
}
