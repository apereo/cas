package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.interrupt.DefaultInterruptInquiryExecutionPlan;
import org.apereo.cas.interrupt.GroovyScriptInterruptInquirer;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlan;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlanConfigurer;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.interrupt.JsonResourceInterruptInquirer;
import org.apereo.cas.interrupt.RegexAttributeInterruptInquirer;
import org.apereo.cas.interrupt.RestEndpointInterruptInquirer;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * This is {@link CasInterruptConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casInterruptConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasInterruptConfiguration implements InterruptInquiryExecutionPlanConfigurer {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Bean
    @ConditionalOnMissingBean(name = "interruptInquirer")
    public InterruptInquiryExecutionPlan interruptInquirer(final List<InterruptInquiryExecutionPlanConfigurer> configurers) {
        val plan = new DefaultInterruptInquiryExecutionPlan();
        configurers.forEach(c -> {
            val name = RegExUtils.removePattern(c.getClass().getSimpleName(), "\\$.+");
            LOGGER.debug("Registering interrupt inquirer [{}]", name);
            c.configureInterruptInquiryExecutionPlan(plan);
        });
        return plan;
    }

    @Override
    public void configureInterruptInquiryExecutionPlan(final InterruptInquiryExecutionPlan plan) {
        val ip = casProperties.getInterrupt();
        if (StringUtils.isNotBlank(ip.getAttributeName()) && StringUtils.isNotBlank(ip.getAttributeValue())) {
            plan.registerInterruptInquirer(new RegexAttributeInterruptInquirer(ip.getAttributeName(), ip.getAttributeValue()));
        }
        if (ip.getJson().getLocation() != null) {
            plan.registerInterruptInquirer(new JsonResourceInterruptInquirer(ip.getJson().getLocation()));
        }
        if (ip.getGroovy().getLocation() != null) {
            plan.registerInterruptInquirer(new GroovyScriptInterruptInquirer(ip.getGroovy().getLocation()));
        }
        if (StringUtils.isNotBlank(ip.getRest().getUrl())) {
            plan.registerInterruptInquirer(new RestEndpointInterruptInquirer(ip.getRest()));
        }
        plan.registerInterruptInquirer((authentication, registeredService, service, credential, requestContext) -> new InterruptResponse());
    }
}
