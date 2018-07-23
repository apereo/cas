package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.security.CasWebSecurityConfigurerAdapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * This is {@link CasWebAppSecurityConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration("casWebAppSecurityConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasWebAppSecurityConfiguration implements WebMvcConfigurer {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private SecurityProperties securityProperties;

    @Bean
    @ConditionalOnMissingBean(name = "casWebSecurityConfigurerAdapter")
    @ConditionalOnProperty(name = "cas.monitor.endpoints.enableEndpointSecurity", matchIfMissing = true)
    public WebSecurityConfigurerAdapter casWebSecurityConfigurerAdapter() {
        return new CasWebSecurityConfigurerAdapter(casProperties, securityProperties);
    }

    @Override
    public void addViewControllers(final ViewControllerRegistry registry) {
        registry.addViewController("/adminlogin").setViewName("casAdminLoginView");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }
}
