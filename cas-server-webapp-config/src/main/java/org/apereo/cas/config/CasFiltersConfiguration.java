package org.apereo.cas.config;

import org.apache.commons.lang3.BooleanUtils;
import org.apereo.cas.configuration.model.core.web.security.HttpWebRequestProperties;
import org.apereo.cas.security.RequestParameterPolicyEnforcementFilter;
import org.apereo.cas.security.ResponseHeadersEnforcementFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
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
public class CasFiltersConfiguration {

    @Autowired
    private HttpWebRequestProperties httpWebRequestProperties;

    /**
     * Character encoding filter character encoding filter.
     *
     * @return the character encoding filter
     */
    @RefreshScope
    @Bean
    public FilterRegistrationBean characterEncodingFilter() {
        final FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new CharacterEncodingFilter(httpWebRequestProperties.getWeb().getEncoding(),
                httpWebRequestProperties.getWeb().isForceEncoding()));
        bean.setUrlPatterns(Collections.singleton("/*"));
        bean.setName("characterEncodingFilter");
        return bean;
    }

    /**
     * Rsponse headers security filter response headers enforcement filter.
     *
     * @return the response headers enforcement filter
     */
    @RefreshScope
    @Bean
    public FilterRegistrationBean responseHeadersSecurityFilter() {
        final Map<String, String> initParams = new HashMap<>();
        initParams.put("enableCacheControl",
                BooleanUtils.toStringTrueFalse(httpWebRequestProperties.getHeader().isCache()));
        initParams.put("enableXContentTypeOptions",
                BooleanUtils.toStringTrueFalse(httpWebRequestProperties.getHeader().isXcontent()));
        initParams.put("enableStrictTransportSecurity",
                BooleanUtils.toStringTrueFalse(httpWebRequestProperties.getHeader().isHsts()));
        initParams.put("enableXFrameOptions",
                BooleanUtils.toStringTrueFalse(httpWebRequestProperties.getHeader().isXframe()));
        initParams.put("enableXSSProtection",
                BooleanUtils.toStringTrueFalse(httpWebRequestProperties.getHeader().isXss()));
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
                httpWebRequestProperties.getParamsToCheck());
        initParams.put(RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID, "none");
        initParams.put(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS,
                BooleanUtils.toStringTrueFalse(httpWebRequestProperties.isAllowMultiValueParameters()));
        initParams.put(RequestParameterPolicyEnforcementFilter.ONLY_POST_PARAMETERS,
                httpWebRequestProperties.getOnlyPostParams());

        final FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new RequestParameterPolicyEnforcementFilter());
        bean.setUrlPatterns(Collections.singleton("/*"));
        bean.setName("requestParameterSecurityFilter");
        bean.setInitParameters(initParams);
        return bean;
    }
}
