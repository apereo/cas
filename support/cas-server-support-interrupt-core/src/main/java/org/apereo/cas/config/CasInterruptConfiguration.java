package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.interrupt.DefaultInterruptInquiryExecutionPlan;
import org.apereo.cas.interrupt.GroovyScriptInterruptInquirer;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlan;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlanConfigurer;
import org.apereo.cas.interrupt.JsonResourceInterruptInquirer;
import org.apereo.cas.interrupt.RegexAttributeInterruptInquirer;
import org.apereo.cas.interrupt.RestEndpointInterruptInquirer;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.spring.boot.ConditionalOnMissingGraalVMNativeImage;
import org.apereo.cas.web.InterruptCookieRetrievingCookieGenerator;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
 * This is {@link CasInterruptConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.InterruptNotifications)
@AutoConfiguration
public class CasInterruptConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "interruptCookieGenerator")
    public CasCookieBuilder interruptCookieGenerator(final CasConfigurationProperties casProperties) {
        val props = casProperties.getInterrupt().getCookie();
        return new InterruptCookieRetrievingCookieGenerator(CookieUtils.buildCookieGenerationContext(props));
    }

    @Configuration(value = "CasInterruptInquiryConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasInterruptInquiryConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "interruptInquirer")
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
    public static class CasInterruptPlansConfiguration {
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
