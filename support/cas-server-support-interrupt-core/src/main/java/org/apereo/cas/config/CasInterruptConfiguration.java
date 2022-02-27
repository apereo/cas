package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.CasFeatureModule;
import org.apereo.cas.interrupt.DefaultInterruptInquiryExecutionPlan;
import org.apereo.cas.interrupt.GroovyScriptInterruptInquirer;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlan;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlanConfigurer;
import org.apereo.cas.interrupt.JsonResourceInterruptInquirer;
import org.apereo.cas.interrupt.RegexAttributeInterruptInquirer;
import org.apereo.cas.interrupt.RestEndpointInterruptInquirer;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeature;
import org.apereo.cas.web.InterruptCookieRetrievingCookieGenerator;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.CookieUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;

import java.util.List;

/**
 * This is {@link CasInterruptConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "CasInterruptConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeature(feature = CasFeatureModule.FeatureCatalog.InterruptNotifications)
public class CasInterruptConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "interruptInquirer")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public InterruptInquiryExecutionPlan interruptInquirer(final List<InterruptInquiryExecutionPlanConfigurer> configurers) {
        val plan = new DefaultInterruptInquiryExecutionPlan();
        configurers
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .forEach(c -> {
                LOGGER.debug("Registering interrupt inquirer [{}]", c.getName());
                c.configureInterruptInquiryExecutionPlan(plan);
            });
        return plan;
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "jsonInterruptInquiryExecutionPlanConfigurer")
    public InterruptInquiryExecutionPlanConfigurer jsonInterruptInquiryExecutionPlanConfigurer(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) throws Exception {
        return BeanSupplier.of(InterruptInquiryExecutionPlanConfigurer.class)
            .when(BeanCondition.on("cas.interrupt.json.location").exists().given(applicationContext.getEnvironment()))
            .supply(() -> plan -> plan.registerInterruptInquirer(new JsonResourceInterruptInquirer(casProperties.getInterrupt().getJson().getLocation())))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "groovyInterruptInquiryExecutionPlanConfigurer")
    public InterruptInquiryExecutionPlanConfigurer groovyInterruptInquiryExecutionPlanConfigurer(
        final ConfigurableApplicationContext applicationContext,
        final CasConfigurationProperties casProperties) throws Exception {
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
        final CasConfigurationProperties casProperties) throws Exception {
        return BeanSupplier.of(InterruptInquiryExecutionPlanConfigurer.class)
            .when(BeanCondition.on("cas.interrupt.regex.attribute-name").given(applicationContext.getEnvironment()))
            .and(BeanCondition.on("cas.interrupt.regex.attribute-value").given(applicationContext.getEnvironment()))
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
        final CasConfigurationProperties casProperties) throws Exception {
        return BeanSupplier.of(InterruptInquiryExecutionPlanConfigurer.class)
            .when(BeanCondition.on("cas.interrupt.rest.url").isUrl().given(applicationContext.getEnvironment()))
            .supply(() -> plan -> plan.registerInterruptInquirer(new RestEndpointInterruptInquirer(casProperties.getInterrupt().getRest())))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "interruptCookieGenerator")
    public CasCookieBuilder interruptCookieGenerator(final CasConfigurationProperties casProperties) {
        val props = casProperties.getInterrupt().getCookie();
        return new InterruptCookieRetrievingCookieGenerator(CookieUtils.buildCookieGenerationContext(props));
    }
}
