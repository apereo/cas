package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.interrupt.DefaultInterruptInquiryExecutionPlan;
import org.apereo.cas.interrupt.GroovyScriptInterruptInquirer;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlan;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlanConfigurer;
import org.apereo.cas.interrupt.JsonResourceInterruptInquirer;
import org.apereo.cas.interrupt.RegexAttributeInterruptInquirer;
import org.apereo.cas.interrupt.RestEndpointInterruptInquirer;
import org.apereo.cas.web.InterruptCookieRetrievingCookieGenerator;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.CookieUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * This is {@link CasInterruptConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration(value = "casInterruptConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasInterruptConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Bean
    @ConditionalOnMissingBean(name = "interruptInquirer")
    @RefreshScope
    public InterruptInquiryExecutionPlan interruptInquirer(final List<InterruptInquiryExecutionPlanConfigurer> configurers) {
        val plan = new DefaultInterruptInquiryExecutionPlan();
        configurers.forEach(c -> {
            LOGGER.debug("Registering interrupt inquirer [{}]", c.getName());
            c.configureInterruptInquiryExecutionPlan(plan);
        });
        return plan;
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "jsonInterruptInquiryExecutionPlanConfigurer")
    @ConditionalOnProperty(name = "cas.interrupt.json.location")
    public InterruptInquiryExecutionPlanConfigurer jsonInterruptInquiryExecutionPlanConfigurer() {
        return plan -> plan.registerInterruptInquirer(new JsonResourceInterruptInquirer(casProperties.getInterrupt().getJson().getLocation()));
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "groovyInterruptInquiryExecutionPlanConfigurer")
    @ConditionalOnProperty(name = "cas.interrupt.groovy.location")
    public InterruptInquiryExecutionPlanConfigurer groovyInterruptInquiryExecutionPlanConfigurer() {
        return plan -> plan.registerInterruptInquirer(new GroovyScriptInterruptInquirer(casProperties.getInterrupt().getGroovy().getLocation()));
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "regexInterruptInquiryExecutionPlanConfigurer")
    @ConditionalOnProperty(name = {"cas.interrupt.regex.attribute-name", "cas.interrupt.regex.attribute-value"})
    public InterruptInquiryExecutionPlanConfigurer regexInterruptInquiryExecutionPlanConfigurer() {
        return plan -> {
            val regex = casProperties.getInterrupt().getRegex();
            plan.registerInterruptInquirer(new RegexAttributeInterruptInquirer(regex.getAttributeName(), regex.getAttributeValue()));
        };
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "restInterruptInquiryExecutionPlanConfigurer")
    @ConditionalOnProperty(name = "cas.interrupt.rest.url")
    public InterruptInquiryExecutionPlanConfigurer restInterruptInquiryExecutionPlanConfigurer() {
        return plan -> plan.registerInterruptInquirer(new RestEndpointInterruptInquirer(casProperties.getInterrupt().getRest()));
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "interruptCookieGenerator")
    public CasCookieBuilder interruptCookieGenerator() {
        val props = casProperties.getInterrupt().getCookie();
        return new InterruptCookieRetrievingCookieGenerator(CookieUtils.buildCookieGenerationContext(props));
    }
}
