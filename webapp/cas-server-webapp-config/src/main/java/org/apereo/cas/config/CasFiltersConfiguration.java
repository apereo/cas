package org.apereo.cas.config;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.web.security.HttpWebRequestProperties;
import org.apereo.cas.security.RequestParameterPolicyEnforcementFilter;
import org.apereo.cas.security.ResponseHeadersEnforcementFilter;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.AuthenticationCredentialsLocalBinderClearingFilter;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Map;

/**
 * This is {@link CasFiltersConfiguration} that attempts to create Spring-managed beans
 * backed by external configuration.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("casFiltersConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasFiltersConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @RefreshScope
    @Bean
    public FilterRegistrationBean characterEncodingFilter() {
        final FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new CharacterEncodingFilter(
                casProperties.getHttpWebRequest().getWeb().getEncoding(),
                casProperties.getHttpWebRequest().getWeb().isForceEncoding()));
        bean.setUrlPatterns(CollectionUtils.wrap("/*"));
        bean.setName("characterEncodingFilter");
        bean.setAsyncSupported(true);
        return bean;
    }

    @ConditionalOnProperty(prefix = "cas.httpWebRequest.cors", name = "enabled", havingValue = "true")
    @Bean
    @RefreshScope
    public FilterRegistrationBean casCorsFilter() {
        final HttpWebRequestProperties.Cors cors = casProperties.getHttpWebRequest().getCors();
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(cors.isEnabled());
        config.setAllowedOrigins(cors.getAllowOrigins());
        config.setAllowedMethods(cors.getAllowMethods());
        config.setAllowedHeaders(cors.getAllowHeaders());
        config.setMaxAge(cors.getMaxAge());
        config.setExposedHeaders(cors.getExposedHeaders());
        source.registerCorsConfiguration("/**", config);
        final FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
        bean.setName("casCorsFilter");
        bean.setAsyncSupported(true);
        bean.setOrder(0);
        return bean;
    }

    @RefreshScope
    @Bean
    public FilterRegistrationBean responseHeadersSecurityFilter() {
        final HttpWebRequestProperties.Header header = casProperties.getHttpWebRequest().getHeader();
        final Map<String, String> initParams = new HashMap<>();
        initParams.put("enableCacheControl", BooleanUtils.toStringTrueFalse(header.isCache()));
        initParams.put("enableXContentTypeOptions", BooleanUtils.toStringTrueFalse(header.isXcontent()));
        initParams.put("enableStrictTransportSecurity", BooleanUtils.toStringTrueFalse(header.isHsts()));
        initParams.put("enableXFrameOptions", BooleanUtils.toStringTrueFalse(header.isXframe()));
        initParams.put("enableXSSProtection", BooleanUtils.toStringTrueFalse(header.isXss()));
        if (StringUtils.isNotBlank(header.getContentSecurityPolicy())) {
            initParams.put("contentSecurityPolicy", header.getContentSecurityPolicy());
        }
        final FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new ResponseHeadersEnforcementFilter());
        bean.setUrlPatterns(CollectionUtils.wrap("/*"));
        bean.setInitParameters(initParams);
        bean.setName("responseHeadersSecurityFilter");
        bean.setAsyncSupported(true);
        return bean;

    }

    @RefreshScope
    @Bean
    public FilterRegistrationBean requestParameterSecurityFilter() {
        final Map<String, String> initParams = new HashMap<>();
        initParams.put(RequestParameterPolicyEnforcementFilter.PARAMETERS_TO_CHECK,
                casProperties.getHttpWebRequest().getParamsToCheck());
        initParams.put(RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID, "none");
        initParams.put(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS,
                BooleanUtils.toStringTrueFalse(casProperties.getHttpWebRequest().isAllowMultiValueParameters()));
        initParams.put(RequestParameterPolicyEnforcementFilter.ONLY_POST_PARAMETERS,
                casProperties.getHttpWebRequest().getOnlyPostParams());

        final FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new RequestParameterPolicyEnforcementFilter());
        bean.setUrlPatterns(CollectionUtils.wrap("/*"));
        bean.setName("requestParameterSecurityFilter");
        bean.setInitParameters(initParams);
        bean.setAsyncSupported(true);
        return bean;
    }

    @Bean
    public FilterRegistrationBean currentCredentialsAndAuthenticationClearingFilter() {
        final FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new AuthenticationCredentialsLocalBinderClearingFilter());
        bean.setUrlPatterns(CollectionUtils.wrap("/*"));
        bean.setName("currentCredentialsAndAuthenticationClearingFilter");
        bean.setAsyncSupported(true);
        return bean;
    }
}
