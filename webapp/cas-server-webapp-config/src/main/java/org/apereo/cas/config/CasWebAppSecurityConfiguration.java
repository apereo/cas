package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.ProtocolEndpointWebSecurityConfigurer;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.security.CasWebSecurityConfigurerAdapter;
import org.apereo.cas.web.security.CasWebSecurityExpressionHandler;
import org.apereo.cas.web.security.CasWebSecurityJdbcConfigurerAdapter;
import org.apereo.cas.web.security.flow.PopulateSpringSecurityContextAction;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.webflow.execution.Action;

import java.util.List;

/**
 * This is {@link CasWebAppSecurityConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Configuration(value = "casWebAppSecurityConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class CasWebAppSecurityConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "casWebSecurityExpressionHandler")
    public SecurityExpressionHandler<FilterInvocation> casWebSecurityExpressionHandler() {
        return new CasWebSecurityExpressionHandler();
    }

    @Bean
    public InitializingBean securityContextHolderInitialization() {
        return () -> SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_GLOBAL);
    }

    @Bean
    @ConditionalOnMissingBean(name = CasWebflowConstants.ACTION_ID_POPULATE_SECURITY_CONTEXT)
    public Action populateSpringSecurityContextAction() {
        return new PopulateSpringSecurityContextAction();
    }

    @Configuration(value = "CasWebappCoreSecurityConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasWebappCoreSecurityConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "casWebSecurityConfigurerAdapter")
        @Autowired
        public WebSecurityConfigurerAdapter casWebSecurityConfigurerAdapter(
            final ObjectProvider<PathMappedEndpoints> pathMappedEndpoints,
            final List<ProtocolEndpointWebSecurityConfigurer> configurersList,
            final SecurityProperties securityProperties,
            final CasConfigurationProperties casProperties,
            @Qualifier("casWebSecurityExpressionHandler")
            final SecurityExpressionHandler<FilterInvocation> casWebSecurityExpressionHandler) {
            return new CasWebSecurityConfigurerAdapter(casProperties, securityProperties,
                casWebSecurityExpressionHandler, pathMappedEndpoints, configurersList);
        }

        @ConditionalOnProperty(name = "cas.monitor.endpoints.jdbc.query")
        @Bean
        @ConditionalOnMissingBean(name = "casWebSecurityConfigurerJdbcAdapter")
        @Autowired
        public CasWebSecurityJdbcConfigurerAdapter casWebSecurityConfigurerJdbcAdapter(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            return new CasWebSecurityJdbcConfigurerAdapter(casProperties, applicationContext);
        }

        @Bean
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
}
