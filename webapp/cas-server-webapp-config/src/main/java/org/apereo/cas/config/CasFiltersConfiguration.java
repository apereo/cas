package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.security.AddResponseHeadersFilter;
import org.apereo.cas.security.RequestParameterPolicyEnforcementFilter;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.web.support.RegisteredServiceResponseHeadersEnforcementFilter;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.AuthenticationCredentialsThreadLocalBinderClearingFilter;

import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
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
@Configuration("casFiltersConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasFiltersConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("argumentExtractor")
    private ArgumentExtractor argumentExtractor;

    @RefreshScope
    @Bean
    @Lazy
    public FilterRegistrationBean characterEncodingFilter() {
        val bean = new FilterRegistrationBean();
        val web = casProperties.getHttpWebRequest().getWeb();
        bean.setFilter(new CharacterEncodingFilter(web.getEncoding(), web.isForceEncoding()));
        bean.setUrlPatterns(CollectionUtils.wrap("/*"));
        bean.setName("characterEncodingFilter");
        bean.setAsyncSupported(true);
        return bean;
    }

    @RefreshScope
    @Bean
    @Lazy
    public FilterRegistrationBean responseHeadersFilter() {
        val bean = new FilterRegistrationBean();
        val filter = new AddResponseHeadersFilter();
        filter.setHeadersMap(casProperties.getHttpWebRequest().getCustomHeaders());
        bean.setFilter(filter);
        bean.setUrlPatterns(CollectionUtils.wrap("/*"));
        bean.setName("responseHeadersFilter");
        bean.setAsyncSupported(true);
        return bean;
    }


    @ConditionalOnProperty(prefix = "cas.httpWebRequest.cors", name = "enabled", havingValue = "true")
    @Bean
    @RefreshScope
    public FilterRegistrationBean casCorsFilter() {
        val cors = casProperties.getHttpWebRequest().getCors();
        val source = new UrlBasedCorsConfigurationSource();
        val config = new CorsConfiguration();
        config.setAllowCredentials(cors.isEnabled());
        config.setAllowedOrigins(cors.getAllowOrigins());
        config.setAllowedMethods(cors.getAllowMethods());
        config.setAllowedHeaders(cors.getAllowHeaders());
        config.setMaxAge(cors.getMaxAge());
        config.setExposedHeaders(cors.getExposedHeaders());
        source.registerCorsConfiguration("/**", config);
        val bean = new FilterRegistrationBean(new CorsFilter(source));
        bean.setName("casCorsFilter");
        bean.setAsyncSupported(true);
        bean.setOrder(0);
        return bean;
    }


    @RefreshScope
    @Bean
    public FilterRegistrationBean responseHeadersSecurityFilter() {
        val header = casProperties.getHttpWebRequest().getHeader();
        val initParams = new HashMap<String, String>();
        initParams.put("enableCacheControl", BooleanUtils.toStringTrueFalse(header.isCache()));
        initParams.put("enableXContentTypeOptions", BooleanUtils.toStringTrueFalse(header.isXcontent()));
        initParams.put("enableStrictTransportSecurity", BooleanUtils.toStringTrueFalse(header.isHsts()));
        initParams.put("enableXFrameOptions", BooleanUtils.toStringTrueFalse(header.isXframe()));
        if (header.isXframe()) {
            initParams.put("XFrameOptions", header.getXframeOptions());
        }
        initParams.put("enableXSSProtection", BooleanUtils.toStringTrueFalse(header.isXss()));
        if (header.isXss()) {
            initParams.put("XSSProtection", header.getXssOptions());
        }
        if (StringUtils.isNotBlank(header.getContentSecurityPolicy())) {
            initParams.put("contentSecurityPolicy", header.getContentSecurityPolicy());
        }
        val bean = new FilterRegistrationBean();
        bean.setFilter(new RegisteredServiceResponseHeadersEnforcementFilter(servicesManager, argumentExtractor));
        bean.setUrlPatterns(CollectionUtils.wrap("/*"));
        bean.setInitParameters(initParams);
        bean.setName("responseHeadersSecurityFilter");
        bean.setAsyncSupported(true);
        return bean;

    }

    @RefreshScope
    @Bean
    public FilterRegistrationBean requestParameterSecurityFilter() {
        val initParams = new HashMap<String, String>();
        initParams.put(RequestParameterPolicyEnforcementFilter.PARAMETERS_TO_CHECK,
            casProperties.getHttpWebRequest().getParamsToCheck());
        initParams.put(RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID, "none");
        initParams.put(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS,
            BooleanUtils.toStringTrueFalse(casProperties.getHttpWebRequest().isAllowMultiValueParameters()));
        initParams.put(RequestParameterPolicyEnforcementFilter.ONLY_POST_PARAMETERS,
            casProperties.getHttpWebRequest().getOnlyPostParams());

        val bean = new FilterRegistrationBean();
        bean.setFilter(new RequestParameterPolicyEnforcementFilter());
        bean.setUrlPatterns(CollectionUtils.wrap("/*"));
        bean.setName("requestParameterSecurityFilter");
        bean.setInitParameters(initParams);
        bean.setAsyncSupported(true);
        return bean;
    }

    @Bean
    public FilterRegistrationBean currentCredentialsAndAuthenticationClearingFilter() {
        val bean = new FilterRegistrationBean();
        bean.setFilter(new AuthenticationCredentialsThreadLocalBinderClearingFilter());
        bean.setUrlPatterns(CollectionUtils.wrap("/*"));
        bean.setName("currentCredentialsAndAuthenticationClearingFilter");
        bean.setAsyncSupported(true);
        return bean;
    }
}
