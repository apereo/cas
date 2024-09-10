package org.apereo.cas.config;

import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.web.CasWebSecurityConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.security.CasWebSecurityConfigurerAdapter;
import org.apereo.cas.web.security.CasWebflowSecurityContextRepository;

import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoExtractionOptions;
import org.apereo.inspektr.common.web.ClientInfoThreadLocalFilter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.webflow.context.servlet.FlowUrlHandler;
import org.springframework.webflow.executor.FlowExecutor;

import jakarta.annotation.Nonnull;

import java.util.List;

/**
 * This is {@link CasWebSecurityConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.WebApplication)
@Configuration(value = "CasWebSecurityConfiguration", proxyBeanMethods = false)
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@EnableWebSecurity
class CasWebSecurityConfiguration {

    @Bean
    @Lazy(false)
    public InitializingBean securityContextHolderInitialization() {
        return () -> SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_THREADLOCAL);
    }

    @Configuration(value = "CasWebAppSecurityMvcConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasWebAppSecurityMvcConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "casWebAppSecurityWebMvcConfigurer")
        public WebMvcConfigurer casWebAppSecurityWebMvcConfigurer(final CasConfigurationProperties casProperties) {
            return new WebMvcConfigurer() {
                @Override
                public void addViewControllers(@Nonnull final ViewControllerRegistry registry) {
                    if (casProperties.getMonitor().getEndpoints().isFormLoginEnabled()) {
                        registry.addViewController(CasWebSecurityConfigurer.ENDPOINT_URL_ADMIN_FORM_LOGIN)
                            .setViewName(CasWebflowConstants.VIEW_ID_ENDPOINT_ADMIN_LOGIN_VIEW);
                        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
                    }
                }
            };
        }
    }

    @Configuration(value = "CasWebappCoreSecurityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CasWebappCoreSecurityConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "securityContextRepository")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SecurityContextRepository securityContextRepository(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("loginFlowUrlHandler")
            final FlowUrlHandler loginFlowUrlHandler,
            @Qualifier("loginFlowExecutor")
            final FlowExecutor loginFlowExecutor) {
            return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository(),
                new CasWebflowSecurityContextRepository(applicationContext)
            );
        }

        @Bean
        @ConditionalOnMissingBean(name = "casClientInfoLoggingFilter")
        public FilterRegistrationBean<ClientInfoThreadLocalFilter> casClientInfoLoggingFilter(
            final CasConfigurationProperties casProperties) {
            val bean = new FilterRegistrationBean<ClientInfoThreadLocalFilter>();
            val audit = casProperties.getAudit().getEngine();
            val options = ClientInfoExtractionOptions.builder()
                .alternateLocalAddrHeaderName(audit.getAlternateClientAddrHeaderName())
                .alternateServerAddrHeaderName(audit.getAlternateServerAddrHeaderName())
                .useServerHostAddress(audit.isUseServerHostAddress())
                .httpRequestHeaders(audit.getHttpRequestHeaders())
                .build();
            bean.setFilter(new ClientInfoThreadLocalFilter(options));
            bean.setUrlPatterns(CollectionUtils.wrap("/*"));
            bean.setName("CAS Client Info Logging Filter");
            bean.setAsyncSupported(true);
            bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
            return bean;
        }

        @Bean
        public FilterRegistrationBean<SecurityContextHolderFilter> securityContextHolderFilter(
            @Qualifier("securityContextRepository")
            final SecurityContextRepository securityContextRepository) {
            val bean = new FilterRegistrationBean<SecurityContextHolderFilter>();
            bean.setFilter(new SecurityContextHolderFilter(securityContextRepository));
            bean.setUrlPatterns(CollectionUtils.wrap("/*"));
            bean.setName("Spring Security Context Holder Filter");
            bean.setAsyncSupported(true);
            bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
            return bean;
        }

        @Bean
        @ConditionalOnMissingBean(name = "casWebSecurityCustomizer")
        public WebSecurityCustomizer casWebSecurityCustomizer(
            @Qualifier("securityContextRepository")
            final SecurityContextRepository securityContextRepository,
            final ObjectProvider<PathMappedEndpoints> pathMappedEndpoints,
            final List<CasWebSecurityConfigurer> configurersList,
            final WebEndpointProperties webEndpointProperties,
            final CasConfigurationProperties casProperties,
            final WebProperties webProperties) {
            val adapter = new CasWebSecurityConfigurerAdapter(casProperties,
                webEndpointProperties, pathMappedEndpoints, configurersList,
                securityContextRepository, webProperties);
            return adapter::configureWebSecurity;
        }

        @Bean
        @ConditionalOnMissingBean(name = "casWebSecurityConfigurerAdapter")
        public SecurityFilterChain casWebSecurityConfigurerAdapter(
            @Qualifier("securityContextRepository")
            final SecurityContextRepository securityContextRepository,
            final HttpSecurity http,
            final ObjectProvider<PathMappedEndpoints> pathMappedEndpoints,
            final List<CasWebSecurityConfigurer> configurersList,
            final WebEndpointProperties webEndpointProperties,
            final SecurityProperties securityProperties,
            final CasConfigurationProperties casProperties,
            final WebProperties webProperties) throws Exception {
            val adapter = new CasWebSecurityConfigurerAdapter(casProperties,
                webEndpointProperties, pathMappedEndpoints, configurersList,
                securityContextRepository, webProperties);
            return adapter.configureHttpSecurity(http).build();
        }
    }

    @Configuration(value = "CasWebAppSecurityJdbcConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnProperty(name = "cas.monitor.endpoints.jdbc.query")
    @SuppressWarnings("ConditionalOnProperty")
    static class CasWebAppSecurityJdbcConfiguration {
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
