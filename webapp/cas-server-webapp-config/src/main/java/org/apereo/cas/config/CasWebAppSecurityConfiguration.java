package org.apereo.cas.config;

import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.ProtocolEndpointWebSecurityConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.security.CasWebSecurityConfigurerAdapter;

import lombok.val;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * This is {@link CasWebAppSecurityConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.WebApplication)
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@AutoConfiguration
@EnableWebSecurity
public class CasWebAppSecurityConfiguration extends GlobalMethodSecurityConfiguration {

    @Bean
    public InitializingBean securityContextHolderInitialization() {
        return () -> SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_THREADLOCAL);
    }

    @Configuration(value = "CasWebAppSecurityMvcConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasWebAppSecurityMvcConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "casWebAppSecurityWebMvcConfigurer")
        public WebMvcConfigurer casWebAppSecurityWebMvcConfigurer() {
            return new WebMvcConfigurer() {
                @Override
                public void addViewControllers(final ViewControllerRegistry registry) {
                    registry.addViewController(CasWebSecurityConfigurerAdapter.ENDPOINT_URL_ADMIN_FORM_LOGIN)
                        .setViewName(CasWebflowConstants.VIEW_ID_ENDPOINT_ADMIN_LOGIN_VIEW);
                    registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
                }
            };
        }
    }

    @Configuration(value = "CasWebappCoreSecurityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasWebappCoreSecurityConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "casWebSecurityCustomizer")
        public WebSecurityCustomizer casWebSecurityCustomizer(
            final ObjectProvider<PathMappedEndpoints> pathMappedEndpoints,
            final List<ProtocolEndpointWebSecurityConfigurer> configurersList,
            final SecurityProperties securityProperties,
            final CasConfigurationProperties casProperties) {
            val adapter = new CasWebSecurityConfigurerAdapter(casProperties, securityProperties,
                pathMappedEndpoints, configurersList);
            return adapter::configureWebSecurity;
        }

        @Bean
        @ConditionalOnMissingBean(name = "casWebSecurityConfigurerAdapter")
        public SecurityFilterChain casWebSecurityConfigurerAdapter(
            final HttpSecurity http,
            final ObjectProvider<PathMappedEndpoints> pathMappedEndpoints,
            final List<ProtocolEndpointWebSecurityConfigurer> configurersList,
            final SecurityProperties securityProperties,
            final CasConfigurationProperties casProperties) throws Exception {
            val adapter = new CasWebSecurityConfigurerAdapter(casProperties, securityProperties,
                pathMappedEndpoints, configurersList);
            return adapter.configureHttpSecurity(http).build();
        }
    }

    @Configuration(value = "CasWebAppSecurityJdbcConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnProperty(name = "cas.monitor.endpoints.jdbc.query")
    @SuppressWarnings("ConditionalOnProperty")
    public static class CasWebAppSecurityJdbcConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "jdbcUserDetailsPasswordEncoder")
        public static PasswordEncoder jdbcUserDetailsPasswordEncoder(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            val jdbc = casProperties.getMonitor().getEndpoints().getJdbc();
            return PasswordEncoderUtils.newPasswordEncoder(jdbc.getPasswordEncoder(), applicationContext);
        }

        @Bean
        @ConditionalOnMissingBean(name = "jdbcUserDetailsManager")
        public UserDetailsManager jdbcUserDetailsManager(
            final CasConfigurationProperties casProperties) {
            val jdbc = casProperties.getMonitor().getEndpoints().getJdbc();
            val manager = new JdbcUserDetailsManager(JpaBeans.newDataSource(jdbc));
            manager.setRolePrefix(jdbc.getRolePrefix());
            manager.setUsersByUsernameQuery(jdbc.getQuery());
            return manager;
        }
    }
}
