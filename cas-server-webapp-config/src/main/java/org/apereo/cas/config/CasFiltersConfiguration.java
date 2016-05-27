package org.apereo.cas.config;

import org.apache.commons.lang3.BooleanUtils;
import org.apereo.cas.security.RequestParameterPolicyEnforcementFilter;
import org.apereo.cas.security.ResponseHeadersEnforcementFilter;
import org.springframework.beans.factory.annotation.Value;
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

    /**
     * The Encoding.
     */
    @Value("${httprequest.web.encoding:UTF-8}")
    private String encoding;

    /**
     * The Force encoding.
     */
    @Value("${httprequest.web.encoding.force:true}")
    private boolean forceEncoding;

    /**
     * The Header cache.
     */
    @Value("${httpresponse.header.cache:false}")
    private boolean headerCache;

    /**
     * The Header hsts.
     */
    @Value("${httpresponse.header.hsts:false}")
    private boolean headerHsts;

    /**
     * The Header xframe.
     */
    @Value("${httpresponse.header.xframe:false}")
    private boolean headerXframe;

    /**
     * The Header xcontent.
     */
    @Value("${httpresponse.header.xcontent:false}")
    private boolean headerXcontent;

    /**
     * The Header xss.
     */
    @Value("${httpresponse.header.xss:false}")
    private boolean headerXss;

    /**
     * The Allow multi value parameters.
     */
    @Value("${cas.http.allow.multivalue.params:false}")
    private boolean allowMultiValueParameters;

    /**
     * The Only post params.
     */
    @Value("${cas.http.allow.post.params:username,password}")
    private String onlyPostParams;

    /**
     * The Params to check.
     */
    @Value("${cas.http.check.params:"
            + "ticket,service,renew,gateway,warn,method,target,SAMLart,pgtUrl,pgt,pgtId,pgtIou,targetService,entityId,token}")
    private String paramsToCheck;

    /**
     * Character encoding filter character encoding filter.
     *
     * @return the character encoding filter
     */
    @RefreshScope
    @Bean(name = "characterEncodingFilter")
    public FilterRegistrationBean characterEncodingFilter() {
        final FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new CharacterEncodingFilter(this.encoding, this.forceEncoding));
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
    @Bean(name = "responseHeadersSecurityFilter")
    public FilterRegistrationBean responseHeadersSecurityFilter() {
        final Map<String, String> initParams = new HashMap<>();
        initParams.put("enableCacheControl", BooleanUtils.toStringTrueFalse(this.headerCache));
        initParams.put("enableXContentTypeOptions", BooleanUtils.toStringTrueFalse(this.headerXcontent));
        initParams.put("enableStrictTransportSecurity", BooleanUtils.toStringTrueFalse(this.headerHsts));
        initParams.put("enableXFrameOptions", BooleanUtils.toStringTrueFalse(this.headerXframe));
        initParams.put("enableXSSProtection", BooleanUtils.toStringTrueFalse(this.headerXss));
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
    @Bean(name = "requestParameterSecurityFilter")
    public FilterRegistrationBean requestParameterSecurityFilter() {
        final Map<String, String> initParams = new HashMap<>();
        initParams.put(RequestParameterPolicyEnforcementFilter.PARAMETERS_TO_CHECK, this.paramsToCheck);
        initParams.put(RequestParameterPolicyEnforcementFilter.CHARACTERS_TO_FORBID, "none");
        initParams.put(RequestParameterPolicyEnforcementFilter.ALLOW_MULTI_VALUED_PARAMETERS, 
                BooleanUtils.toStringTrueFalse(this.allowMultiValueParameters));
        initParams.put(RequestParameterPolicyEnforcementFilter.ONLY_POST_PARAMETERS, this.onlyPostParams);

        final FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new RequestParameterPolicyEnforcementFilter());
        bean.setUrlPatterns(Collections.singleton("/*"));
        bean.setName("requestParameterSecurityFilter");
        bean.setInitParameters(initParams);
        return bean;
    }
}
