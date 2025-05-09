package org.apereo.cas.config;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.model.support.interrupt.InterruptCookieProperties;
import org.apereo.cas.interrupt.DefaultInterruptInquiryExecutionPlan;
import org.apereo.cas.interrupt.GroovyScriptInterruptInquirer;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlan;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlanConfigurer;
import org.apereo.cas.interrupt.InterruptTrackingCookieCipherExecutor;
import org.apereo.cas.interrupt.InterruptTrackingEngine;
import org.apereo.cas.interrupt.JsonResourceInterruptInquirer;
import org.apereo.cas.interrupt.RegexAttributeInterruptInquirer;
import org.apereo.cas.interrupt.RestEndpointInterruptInquirer;
import org.apereo.cas.interrupt.SimpleInterruptTrackingEngine;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.cipher.DefaultCipherExecutorResolver;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.spring.boot.ConditionalOnMissingGraalVMNativeImage;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.cookie.CookieValueManager;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.gen.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.mgmr.DefaultCasCookieValueManager;
import org.apereo.cas.web.support.mgmr.DefaultCookieSameSitePolicy;
import org.apereo.cas.web.support.mgmr.NoOpCookieValueManager;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import java.util.ArrayList;

/**
 * This is {@link CasInterruptAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.InterruptNotifications)
@AutoConfiguration
public class CasInterruptAutoConfiguration {

    @Configuration(value = "CasInterruptTrackingConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasInterruptTrackingConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "interruptCookieCipherExecutor")
        public CipherExecutor interruptCookieCipherExecutor(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            val props = casProperties.getInterrupt().getCookie();
            var enabled = props.getCrypto().isEnabled();
            if (!enabled && StringUtils.isNotBlank(props.getCrypto().getEncryption().getKey())
                && StringUtils.isNotBlank(props.getCrypto().getSigning().getKey())) {
                LOGGER.warn("Interrupt webflow cookie encryption/signing is not enabled explicitly in the configuration for cookie [{}], yet signing/encryption keys "
                    + "are defined for operations. CAS will proceed to enable the cookie encryption/signing functionality.", props.getName());
                enabled = true;
            }

            if (enabled) {
                return CipherExecutorUtils.newStringCipherExecutor(props.getCrypto(), InterruptTrackingCookieCipherExecutor.class);
            }
            LOGGER.info("Interrupt webflow cookie encryption/signing is turned off and MAY NOT be safe in a production environment. "
                + "Consider using other choices to handle encryption, signing and verification of metadata artifacts");
            return CipherExecutor.noOp();
        }

        @ConditionalOnMissingBean(name = "interruptCookieValueManager")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CookieValueManager interruptCookieValueManager(
            @Qualifier(TenantExtractor.BEAN_NAME)
            final TenantExtractor tenantExtractor,
            @Qualifier(GeoLocationService.BEAN_NAME)
            final ObjectProvider<GeoLocationService> geoLocationService,
            final CasConfigurationProperties casProperties,
            @Qualifier("interruptCookieCipherExecutor")
            final CipherExecutor cookieCipherExecutor) {
            val props = casProperties.getInterrupt().getCookie();
            if (props.getCrypto().isEnabled()) {

                val cipherExecutorResolver = new DefaultCipherExecutorResolver(cookieCipherExecutor, tenantExtractor,
                    InterruptCookieProperties.class, bindingContext -> {
                    val properties = bindingContext.value();
                    return CipherExecutorUtils.newStringCipherExecutor(properties.getInterrupt().getCookie().getCrypto(), InterruptTrackingCookieCipherExecutor.class);
                });
                
                return new DefaultCasCookieValueManager(cipherExecutorResolver, tenantExtractor,
                    geoLocationService, DefaultCookieSameSitePolicy.INSTANCE, props);
            }
            return new NoOpCookieValueManager(tenantExtractor);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "interruptCookieGenerator")
        public CasCookieBuilder interruptCookieGenerator(
            @Qualifier("interruptCookieValueManager")
            final CookieValueManager interruptCookieValueManager,
            final CasConfigurationProperties casProperties) {
            val props = casProperties.getInterrupt().getCookie();
            return new CookieRetrievingCookieGenerator(CookieUtils.buildCookieGenerationContext(props), interruptCookieValueManager);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = InterruptTrackingEngine.BEAN_NAME)
        public InterruptTrackingEngine interruptTrackingEngine(
            @Qualifier("interruptCookieGenerator")
            final CasCookieBuilder interruptCookieGenerator,
            final CasConfigurationProperties casProperties) {
            return new SimpleInterruptTrackingEngine(interruptCookieGenerator, casProperties);
        }
    }


    @Configuration(value = "CasInterruptInquiryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasInterruptInquiryConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = InterruptInquirer.BEAN_NAME)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public InterruptInquiryExecutionPlan interruptInquirer(final ConfigurableApplicationContext applicationContext) {
            val configurers = new ArrayList<>(applicationContext.getBeansOfType(InterruptInquiryExecutionPlanConfigurer.class).values());
            val plan = new DefaultInterruptInquiryExecutionPlan();
            configurers
                .stream()
                .filter(BeanSupplier::isNotProxy)
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .forEach(cfg -> {
                    LOGGER.debug("Registering interrupt inquirer [{}]", cfg.getName());
                    cfg.configureInterruptInquiryExecutionPlan(plan);
                });
            return plan;
        }
    }

    @Configuration(value = "CasInterruptPlansConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasInterruptPlansConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "jsonInterruptInquiryExecutionPlanConfigurer")
        public InterruptInquiryExecutionPlanConfigurer jsonInterruptInquiryExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(InterruptInquiryExecutionPlanConfigurer.class)
                .when(BeanCondition.on("cas.interrupt.json.location").exists().given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerInterruptInquirer(new JsonResourceInterruptInquirer(casProperties.getInterrupt().getJson().getLocation())))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "groovyInterruptInquiryExecutionPlanConfigurer")
        @ConditionalOnMissingGraalVMNativeImage
        public InterruptInquiryExecutionPlanConfigurer groovyInterruptInquiryExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(InterruptInquiryExecutionPlanConfigurer.class)
                .when(BeanCondition.on("cas.interrupt.groovy.location").exists().given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerInterruptInquirer(new GroovyScriptInterruptInquirer(casProperties.getInterrupt().getGroovy().getLocation())))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "regexInterruptInquiryExecutionPlanConfigurer")
        public InterruptInquiryExecutionPlanConfigurer regexInterruptInquiryExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(InterruptInquiryExecutionPlanConfigurer.class)
                .when(BeanCondition.on("cas.interrupt.regex.attribute-name")
                    .and("cas.interrupt.regex.attribute-value")
                    .given(applicationContext.getEnvironment()))
                .supply(() -> plan -> {
                    val regex = casProperties.getInterrupt().getRegex();
                    plan.registerInterruptInquirer(new RegexAttributeInterruptInquirer(regex.getAttributeName(), regex.getAttributeValue()));
                })
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "restInterruptInquiryExecutionPlanConfigurer")
        public InterruptInquiryExecutionPlanConfigurer restInterruptInquiryExecutionPlanConfigurer(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(InterruptInquiryExecutionPlanConfigurer.class)
                .when(BeanCondition.on("cas.interrupt.rest.url").isUrl().given(applicationContext.getEnvironment()))
                .supply(() -> plan -> plan.registerInterruptInquirer(new RestEndpointInterruptInquirer(casProperties.getInterrupt().getRest())))
                .otherwiseProxy()
                .get();
        }
    }
}
