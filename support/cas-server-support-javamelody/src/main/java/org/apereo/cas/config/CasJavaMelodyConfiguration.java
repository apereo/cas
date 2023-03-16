package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.extern.slf4j.Slf4j;
import net.bull.javamelody.MonitoringSpringAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * This is {@link CasJavaMelodyConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.6.x
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Monitoring, module = "javamelody")
@AutoConfiguration
public class CasJavaMelodyConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "monitorableComponentsAdvisor")
    public MonitoringSpringAdvisor monitorableComponentsAdvisor() {
        return new MonitoringSpringAdvisor(new AnnotationMatchingPointcut(Monitorable.class, true));
    }
}
