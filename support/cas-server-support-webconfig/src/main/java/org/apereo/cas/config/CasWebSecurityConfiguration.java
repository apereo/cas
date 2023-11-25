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

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoThreadLocalFilter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
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

import javax.annotation.Nonnull;

import java.util.HashMap;
import java.util.List;

/**
 * This is {@link CasWebSecurityConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.WebApplication)
@AutoConfiguration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@EnableWebSecurity
public class CasWebSecurityConfiguration {

    @Bean
    @Lazy(false)
    public InitializingBean securityContextHolderInitialization() {
        return () -> SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_THREADLOCAL);
    }

    @Configuration(value = "CasWebAppSecurityMvcConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasWebAppSecurityMvcConfiguration {
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
    public static class CasWebappCoreSecurityConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "securityContextRepository")
        public SecurityContextRepository securityContextRepository() {
            return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
            );
        }

        @Bean
        @ConditionalOnMissingBean(name = "casClientInfoLoggingFilter")
        public FilterRegistrationBean<ClientInfoThreadLocalFilter> casClientInfoLoggingFilter(
            final CasConfigurationProperties casProperties) {
            val audit = casProperties.getAudit().getEngine();

            val bean = new FilterRegistrationBean<ClientInfoThreadLocalFilter>();
            bean.setFilter(new ClientInfoThreadLocalFilter());
            bean.setUrlPatterns(CollectionUtils.wrap("/*"));
            bean.setName("CAS Client Info Logging Filter");
            bean.setAsyncSupported(true);
            bean.setOrder(Ordered.HIGHEST_PRECEDENCE);

            val initParams = new HashMap<String, String>();
            if (StringUtils.isNotBlank(audit.getAlternateClientAddrHeaderName())) {
                initParams.put(ClientInfoThreadLocalFilter.CONST_IP_ADDRESS_HEADER, audit.getAlternateClientAddrHeaderName());
            }

            if (StringUtils.isNotBlank(audit.getAlternateServerAddrHeaderName())) {
                initParams.put(ClientInfoThreadLocalFilter.CONST_SERVER_IP_ADDRESS_HEADER, audit.getAlternateServerAddrHeaderName());
            }

            initParams.put(ClientInfoThreadLocalFilter.CONST_USE_SERVER_HOST_ADDRESS, String.valueOf(audit.isUseServerHostAddress()));
            bean.setInitParameters(initParams);
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
            bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
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
            final CasConfigurationProperties casProperties) {
            val adapter = new CasWebSecurityConfigurerAdapter(casProperties,
                webEndpointProperties, pathMappedEndpoints, configurersList, securityContextRepository);
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
            final CasConfigurationProperties casProperties) throws Exception {
            val adapter = new CasWebSecurityConfigurerAdapter(casProperties,
                webEndpointProperties, pathMappedEndpoints, configurersList, securityContextRepository);
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
