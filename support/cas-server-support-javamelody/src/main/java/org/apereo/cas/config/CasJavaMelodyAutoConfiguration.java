package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.extern.slf4j.Slf4j;
import net.bull.javamelody.JavaMelodyAutoConfiguration;
import net.bull.javamelody.MonitoringSpringAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * This is {@link CasJavaMelodyAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Monitoring,
                             module = "javamelody")
@AutoConfiguration
@ImportAutoConfiguration(JavaMelodyAutoConfiguration.class)
@Lazy(false)
public class CasJavaMelodyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "monitorableComponentsAdvisor")
    public MonitoringSpringAdvisor monitorableComponentsAdvisor() {
        return new MonitoringSpringAdvisor(new AnnotationMatchingPointcut(Monitorable.class, null));
    }

    @Bean
    public CasWebSecurityConfigurer<HttpSecurity> javaMelodyMonitoringEndpointConfigurer() {
        return new CasWebSecurityConfigurer<>() {

            @Override
            @CanIgnoreReturnValue
            public CasWebSecurityConfigurer<HttpSecurity> configure(final HttpSecurity http) throws Exception {
                http.authorizeHttpRequests(customizer -> customizer.requestMatchers(PathPatternRequestMatcher.withDefaults().matcher("/monitoring")).authenticated())
                    .httpBasic(customizer -> customizer.init(http));
                return this;
            }
        };
    }
}
