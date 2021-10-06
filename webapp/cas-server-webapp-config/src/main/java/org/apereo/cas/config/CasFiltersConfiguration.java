package org.apereo.cas.config;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.support.RegisteredServiceCorsConfigurationSource;
import org.apereo.cas.services.web.support.RegisteredServiceResponseHeadersEnforcementFilter;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.AuthenticationCredentialsThreadLocalBinderClearingFilter;
import org.apereo.cas.web.support.filters.AddResponseHeadersFilter;
import org.apereo.cas.web.support.filters.RequestParameterPolicyEnforcementFilter;
import org.apereo.cas.web.support.filters.ResponseHeadersEnforcementFilter;

import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.CorsFilter;

import java.util.HashMap;

/**
 * This is {@link CasFiltersConfiguration} that attempts to create Spring-managed beans
 * backed by external configuration.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration(value = "casFiltersConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasFiltersConfiguration {

    @Configuration(value = "CasFiltersEncodingConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasFiltersBaseConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public FilterRegistrationBean<CharacterEncodingFilter> characterEncodingFilter(final CasConfigurationProperties casProperties) {
            val bean = new FilterRegistrationBean<CharacterEncodingFilter>();
            val web = casProperties.getHttpWebRequest().getWeb();
            bean.setFilter(new CharacterEncodingFilter(web.getEncoding(), web.isForceEncoding()));
            bean.setUrlPatterns(CollectionUtils.wrap("/*"));
            bean.setName("characterEncodingFilter");
            bean.setAsyncSupported(true);
            return bean;
        }

        @Bean
        public FilterRegistrationBean<AuthenticationCredentialsThreadLocalBinderClearingFilter> currentCredentialsAndAuthenticationClearingFilter() {
            val bean = new FilterRegistrationBean<AuthenticationCredentialsThreadLocalBinderClearingFilter>();
            bean.setFilter(new AuthenticationCredentialsThreadLocalBinderClearingFilter());
            bean.setUrlPatterns(CollectionUtils.wrap("/*"));
            bean.setName("currentCredentialsAndAuthenticationClearingFilter");
            bean.setAsyncSupported(true);
            return bean;
        }

    }

    @Configuration(value = "CasFiltersResponseHeadersConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @AutoConfigureAfter(CasCoreServicesConfiguration.class)
    public static class CasFiltersResponseHeadersConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public FilterRegistrationBean<AddResponseHeadersFilter> responseHeadersFilter(final CasConfigurationProperties casProperties) {
            val bean = new FilterRegistrationBean<AddResponseHeadersFilter>();
            val filter = new AddResponseHeadersFilter();
            filter.setHeadersMap(casProperties.getHttpWebRequest().getCustomHeaders());
            bean.setFilter(filter);
            bean.setUrlPatterns(CollectionUtils.wrap("/*"));
            bean.setName("responseHeadersFilter");
            bean.setAsyncSupported(true);
            return bean;
        }

        @ConditionalOnProperty(prefix = "cas.http-web-request.header", name = "enabled", havingValue = "true", matchIfMissing = true)
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public FilterRegistrationBean<RegisteredServiceResponseHeadersEnforcementFilter> responseHeadersSecurityFilter(
            final CasConfigurationProperties casProperties,
            @Qualifier("argumentExtractor")
            final ArgumentExtractor argumentExtractor,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier("registeredServiceAccessStrategyEnforcer")
            final AuditableExecution registeredServiceAccessStrategyEnforcer,
            @Qualifier(AuthenticationServiceSelectionPlan.BEAN_NAME)
            final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies) {
            val header = casProperties.getHttpWebRequest().getHeader();
            val initParams = new HashMap<String, String>();
            initParams.put(ResponseHeadersEnforcementFilter.INIT_PARAM_ENABLE_CACHE_CONTROL, BooleanUtils.toStringTrueFalse(header.isCache()));
            initParams.put(ResponseHeadersEnforcementFilter.INIT_PARAM_ENABLE_XCONTENT_OPTIONS, BooleanUtils.toStringTrueFalse(header.isXcontent()));
            initParams.put(ResponseHeadersEnforcementFilter.INIT_PARAM_ENABLE_STRICT_TRANSPORT_SECURITY, BooleanUtils.toStringTrueFalse(header.isHsts()));
            initParams.put(ResponseHeadersEnforcementFilter.INIT_PARAM_ENABLE_STRICT_XFRAME_OPTIONS, BooleanUtils.toStringTrueFalse(header.isXframe()));
            initParams.put(ResponseHeadersEnforcementFilter.INIT_PARAM_STRICT_XFRAME_OPTIONS, header.getXframeOptions());
            initParams.put(ResponseHeadersEnforcementFilter.INIT_PARAM_ENABLE_XSS_PROTECTION, BooleanUtils.toStringTrueFalse(header.isXss()));
            initParams.put(ResponseHeadersEnforcementFilter.INIT_PARAM_XSS_PROTECTION, header.getXssOptions());
            if (StringUtils.isNotBlank(header.getContentSecurityPolicy())) {
                initParams.put(ResponseHeadersEnforcementFilter.INIT_PARAM_CONTENT_SECURITY_POLICY, header.getContentSecurityPolicy());
            }
            val bean = new FilterRegistrationBean<RegisteredServiceResponseHeadersEnforcementFilter>();
            bean.setFilter(new RegisteredServiceResponseHeadersEnforcementFilter(servicesManager,
                argumentExtractor, authenticationRequestServiceSelectionStrategies,
                registeredServiceAccessStrategyEnforcer));
            bean.setUrlPatterns(CollectionUtils.wrap("/*"));
            bean.setInitParameters(initParams);
            bean.setName("responseHeadersSecurityFilter");
            bean.setAsyncSupported(true);
            return bean;
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public FilterRegistrationBean<RequestParameterPolicyEnforcementFilter> requestParameterSecurityFilter(
            final CasConfigurationProperties casProperties) {
            val httpWebRequest = casProperties.getHttpWebRequest();
            val initParams = new HashMap<String, String>();
            if (StringUtils.isNotBlank(httpWebRequest.getParamsToCheck())) {
                initParams.put(RequestParameterPolicyEnforcementFilter.PARAMETERS_TO_CHECK,
                    httpWebRequest.getParamsToCheck());
            }
            initParams.put(RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID,
                httpWebRequest.getCharactersToForbid());
            initParams.put(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS,
                BooleanUtils.toStringTrueFalse(httpWebRequest.isAllowMultiValueParameters()));
            initParams.put(RequestParameterPolicyEnforcementFilter.ONLY_POST_PARAMETERS,
                httpWebRequest.getOnlyPostParams());
            initParams.put(RequestParameterPolicyEnforcementFilter.THROW_ON_ERROR, Boolean.TRUE.toString());

            if (StringUtils.isNotBlank(httpWebRequest.getPatternToBlock())) {
                initParams.put(RequestParameterPolicyEnforcementFilter.PATTERN_TO_BLOCK,
                    httpWebRequest.getPatternToBlock());
            }

            val bean = new FilterRegistrationBean<RequestParameterPolicyEnforcementFilter>();
            bean.setFilter(new RequestParameterPolicyEnforcementFilter());
            bean.setUrlPatterns(CollectionUtils.wrap("/*"));
            bean.setName("requestParameterSecurityFilter");
            bean.setInitParameters(initParams);
            bean.setAsyncSupported(true);
            return bean;
        }

    }

    @Configuration(value = "CasFiltersCorsConfiguration", proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "cas.http-web-request.cors", name = "enabled", havingValue = "true")
    public static class CasFiltersCorsConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "corsConfigurationSource")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public CorsConfigurationSource corsConfigurationSource(
            final CasConfigurationProperties casProperties,
            @Qualifier("argumentExtractor")
            final ArgumentExtractor argumentExtractor,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new RegisteredServiceCorsConfigurationSource(casProperties, servicesManager, argumentExtractor);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Autowired
        public FilterRegistrationBean<CorsFilter> casCorsFilter(
            @Qualifier("corsConfigurationSource")
            final CorsConfigurationSource corsConfigurationSource) {
            val bean = new FilterRegistrationBean<>(new CorsFilter(corsConfigurationSource));
            bean.setName("casCorsFilter");
            bean.setAsyncSupported(true);
            bean.setOrder(0);
            return bean;
        }

    }
}
