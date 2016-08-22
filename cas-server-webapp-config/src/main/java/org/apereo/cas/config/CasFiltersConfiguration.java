package org.apereo.cas.config;

import org.apache.commons.lang3.BooleanUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.security.RequestParameterPolicyEnforcementFilter;
import org.apereo.cas.security.ResponseHeadersEnforcementFilter;
import org.apereo.cas.web.support.CurrentCredentialsAndAuthenticationClearingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.Collections;
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

    /**
     * Character encoding filter character encoding filter.
     *
     * @return the character encoding filter
     */
    @RefreshScope
    @Bean
    public FilterRegistrationBean characterEncodingFilter() {
        final FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new CharacterEncodingFilter(
                casProperties.getHttpWebRequest().getWeb().getEncoding(),
                casProperties.getHttpWebRequest().getWeb().isForceEncoding()));
        bean.setUrlPatterns(Collections.singleton("/*"));
        bean.setName("characterEncodingFilter");
        return bean;
    }

    /**
     * Response headers security filter response headers enforcement filter.
     *
     * @return the response headers enforcement filter
     */
    @RefreshScope
    @Bean
    public FilterRegistrationBean responseHeadersSecurityFilter() {
        final Map<String, String> initParams = new HashMap<>();
        initParams.put("enableCacheControl",
                BooleanUtils.toStringTrueFalse(casProperties.getHttpWebRequest().getHeader().isCache()));
        initParams.put("enableXContentTypeOptions",
                BooleanUtils.toStringTrueFalse(casProperties.getHttpWebRequest().getHeader().isXcontent()));
        initParams.put("enableStrictTransportSecurity",
                BooleanUtils.toStringTrueFalse(casProperties.getHttpWebRequest().getHeader().isHsts()));
        initParams.put("enableXFrameOptions",
                BooleanUtils.toStringTrueFalse(casProperties.getHttpWebRequest().getHeader().isXframe()));
        initParams.put("enableXSSProtection",
                BooleanUtils.toStringTrueFalse(casProperties.getHttpWebRequest().getHeader().isXss()));
        final FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new ResponseHeadersEnforcementFilter());
        bean.setUrlPatterns(Collections.singleton("/*"));
        bean.setInitParameters(initParams);
        bean.setName("responseHeadersSecurityFilter");
        return bean;

    }

    /**
     * Request parameter security filter request parameter policy enforcement filter.
     *
     * @return the request parameter policy enforcement filter
     */
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
        bean.setUrlPatterns(Collections.singleton("/*"));
        bean.setName("requestParameterSecurityFilter");
        bean.setInitParameters(initParams);
        return bean;
    }


    /**
     * Current credentials and authentication clearing filter.
     *
     * @return the current credentials and authentication clearing filter
     */
    @Bean
    public FilterRegistrationBean currentCredentialsAndAuthenticationClearingFilter() {
        final FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new CurrentCredentialsAndAuthenticationClearingFilter());
        bean.setUrlPatterns(Collections.singleton("/*"));
        bean.setName("currentCredentialsAndAuthenticationClearingFilter");
        bean.setAsyncSupported(true);
        return bean;
    }
}
