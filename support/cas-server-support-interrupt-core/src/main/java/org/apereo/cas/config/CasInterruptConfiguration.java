package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.interrupt.GroovyScriptInterruptInquirer;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.interrupt.JsonResourceInterruptInquirer;
import org.apereo.cas.interrupt.RegexAttributeInterruptInquirer;
import org.apereo.cas.interrupt.RestEndpointInterruptInquirer;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link CasInterruptConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casInterruptConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasInterruptConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    @ConditionalOnMissingBean(name = "interruptInquirer")
    public InterruptInquirer interruptInquirer() {
        val ip = casProperties.getInterrupt();
        if (StringUtils.isNotBlank(ip.getAttributeName()) && StringUtils.isNotBlank(ip.getAttributeValue())) {
            return new RegexAttributeInterruptInquirer(ip.getAttributeName(), ip.getAttributeValue());
        }
        if (ip.getJson().getLocation() != null) {
            return new JsonResourceInterruptInquirer(ip.getJson().getLocation());
        }
        if (ip.getGroovy().getLocation() != null) {
            return new GroovyScriptInterruptInquirer(ip.getGroovy().getLocation());
        }
        if (StringUtils.isNotBlank(ip.getRest().getUrl())) {
            return new RestEndpointInterruptInquirer(ip.getRest());
        }
        return (authentication, registeredService, service, credential) -> new InterruptResponse();
    }
}
