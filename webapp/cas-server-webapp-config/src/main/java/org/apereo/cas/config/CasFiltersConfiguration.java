package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
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

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("argumentExtractor")
    private ObjectProvider<ArgumentExtractor> argumentExtractor;

    @Autowired
    @Qualifier("authenticationServiceSelectionPlan")
    private ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies;

    @RefreshScope
    @Bean
    public FilterRegistrationBean characterEncodingFilter() {
        val bean = new FilterRegistrationBean<CharacterEncodingFilter>();
        val web = casProperties.getHttpWebRequest().getWeb();
        bean.setFilter(new CharacterEncodingFilter(web.getEncoding(), web.isForceEncoding()));
        bean.setUrlPatterns(CollectionUtils.wrap("/*"));
        bean.setName("characterEncodingFilter");
        bean.setAsyncSupported(true);
        return bean;
    }

    @RefreshScope
    @Bean
    public FilterRegistrationBean responseHeadersFilter() {
        val bean = new FilterRegistrationBean<AddResponseHeadersFilter>();
        val filter = new AddResponseHeadersFilter();
        filter.setHeadersMap(casProperties.getHttpWebRequest().getCustomHeaders());
        bean.setFilter(filter);
        bean.setUrlPatterns(CollectionUtils.wrap("/*"));
        bean.setName("responseHeadersFilter");
        bean.setAsyncSupported(true);
        return bean;
    }

    @ConditionalOnProperty(prefix = "cas.http-web-request.cors", name = "enabled", havingValue = "true")
    @Bean
    @RefreshScope
    public FilterRegistrationBean casCorsFilter() {
        val cors = casProperties.getHttpWebRequest().getCors();
        val source = new UrlBasedCorsConfigurationSource();
        val config = new CorsConfiguration();
        config.setAllowCredentials(cors.isAllowCredentials());
        config.setAllowedOrigins(cors.getAllowOrigins());
        config.setAllowedMethods(cors.getAllowMethods());
        config.setAllowedHeaders(cors.getAllowHeaders());
        config.setMaxAge(cors.getMaxAge());
        config.setExposedHeaders(cors.getExposedHeaders());
        source.registerCorsConfiguration("/**", config);
        val bean = new FilterRegistrationBean<CorsFilter>(new CorsFilter(source));
        bean.setName("casCorsFilter");
        bean.setAsyncSupported(true);
        bean.setOrder(0);
        return bean;
    }

    @ConditionalOnProperty(prefix = "cas.http-web-request.header", name = "enabled", havingValue = "true", matchIfMissing = true)
    @RefreshScope
    @Bean
    public FilterRegistrationBean responseHeadersSecurityFilter() {
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
        bean.setFilter(new RegisteredServiceResponseHeadersEnforcementFilter(servicesManager.getObject(),
            argumentExtractor.getObject(), authenticationRequestServiceSelectionStrategies.getObject()));
        bean.setUrlPatterns(CollectionUtils.wrap("/*"));
        bean.setInitParameters(initParams);
        bean.setName("responseHeadersSecurityFilter");
        bean.setAsyncSupported(true);
        return bean;
    }

    @RefreshScope
    @Bean
    public FilterRegistrationBean requestParameterSecurityFilter() {
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

    @Bean
    public FilterRegistrationBean currentCredentialsAndAuthenticationClearingFilter() {
        val bean = new FilterRegistrationBean<AuthenticationCredentialsThreadLocalBinderClearingFilter>();
        bean.setFilter(new AuthenticationCredentialsThreadLocalBinderClearingFilter());
        bean.setUrlPatterns(CollectionUtils.wrap("/*"));
        bean.setName("currentCredentialsAndAuthenticationClearingFilter");
        bean.setAsyncSupported(true);
        return bean;
    }
}
