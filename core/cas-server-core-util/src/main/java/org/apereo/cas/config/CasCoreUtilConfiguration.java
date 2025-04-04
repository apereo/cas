package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.util.feature.CasRuntimeModuleLoader;
import org.apereo.cas.util.feature.DefaultCasRuntimeModuleLoader;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.Converters;
import org.apereo.cas.util.spring.SpringAwareMessageMessageInterpolator;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.text.DefaultMessageSanitizer;
import org.apereo.cas.util.text.MessageSanitationContributor;
import org.apereo.cas.util.text.MessageSanitizer;
import org.apereo.cas.util.text.TicketCatalogMessageSanitationContributor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.beanvalidation.BeanValidationPostProcessor;
import jakarta.validation.MessageInterpolator;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link CasCoreUtilConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableScheduling
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Core)
@Configuration(value = "CasCoreUtilConfiguration", proxyBeanMethods = false)
class CasCoreUtilConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @Lazy(false)
    public ApplicationContextProvider casApplicationContextProvider() {
        return new ApplicationContextProvider();
    }

    /**
     * Bean bootstrap executor.
     * Enables bean background initialization option: {@code @Bean(bootstrap=BACKGROUND)}
     * for singling out specific beans for background initialization, covering the entire bean
     * creation step for each such bean on context startup.
     * A {@code bootstrapExecutor} bean of type {@link Executor} must be declared for background bootstrapping to
     * be actually active. Otherwise, the background markers will be ignored at runtime.
     * @return the executor
     */
    @Bean
    @Lazy(false)
    public Executor bootstrapExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Configuration(value = "CasCoreUtilContextConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreUtilContextConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Lazy(false)
        public InitializingBean casCoreUtilInitialization(
            final ConfigurableApplicationContext applicationContext,
            final List<Converter> allConverters) {
            return () -> {
                val registry = (ConverterRegistry) DefaultConversionService.getSharedInstance();
                allConverters.forEach(converter -> {
                    registry.addConverter(converter);
                    applicationContext.getEnvironment().getConversionService().addConverter(converter);
                });
            };
        }
    }

    @Configuration(value = "CasCoreUtilConverterConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Lazy(false)
    static class CasCoreUtilConverterConfiguration {
        @Bean
        public static MessageInterpolator messageInterpolator() {
            return new SpringAwareMessageMessageInterpolator();
        }

        @Bean
        public static Converter<ZonedDateTime, String> zonedDateTimeToStringConverter() {
            return new Converters.ZonedDateTimeToStringConverter();
        }

        @Bean
        public static Converter<String, Resource> stringToResourceConverter() {
            return new Converters.StringToResourceConverter();
        }

        @Bean
        public static ObjectMapper objectMapper(final ConfigurableApplicationContext applicationContext) {
            return JacksonObjectMapperFactory
                .builder()
                .applicationContext(applicationContext)
                .build()
                .toObjectMapper();
        }

    }

    @Configuration(value = "CasCoreUtilEssentialConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @Lazy(false)
    static class CasCoreUtilEssentialConfiguration {

        /**
         * Create casBeanValidationPostProcessor bean.
         * Note that {@code BeanPostProcessor} beans should be static.
         *
         * @return the BeanValidationPostProcessor
         */
        @Bean
        @ConditionalOnMissingBean(name = "casBeanValidationPostProcessor")
        public static BeanPostProcessor casBeanValidationPostProcessor() {
            return new BeanValidationPostProcessor();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasRuntimeModuleLoader casRuntimeModuleLoader() {
            return new DefaultCasRuntimeModuleLoader();
        }
    }

    @Configuration(value = "CasCoreMessageSanitationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasCoreMessageSanitationConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "ticketCatalogMessageSanitationContributor")
        public MessageSanitationContributor defaultMessageSanitationContributor(
            @Qualifier(TicketCatalog.BEAN_NAME) final ObjectProvider<TicketCatalog> ticketCatalog) {
            return new TicketCatalogMessageSanitationContributor(ticketCatalog);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "proxyGrantingTicketIouMessageSanitationContributor")
        public MessageSanitationContributor proxyGrantingTicketIouMessageSanitationContributor() {
            return () -> List.of(ProxyGrantingTicket.PROXY_GRANTING_TICKET_IOU_PREFIX);
        }

        @Bean
        @ConditionalOnMissingBean(name = MessageSanitizer.BEAN_NAME)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public MessageSanitizer messageSanitizer(final List<MessageSanitationContributor> contributors) {
            val prefixes = contributors
                .stream()
                .map(MessageSanitationContributor::getTicketIdentifierPrefixes)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.joining("|"));
            val pattern = Pattern.compile("(?:(?:" + prefixes + ")-\\d+-)([\\w.-]+)");
            return new DefaultMessageSanitizer(pattern);
        }
    }
}
