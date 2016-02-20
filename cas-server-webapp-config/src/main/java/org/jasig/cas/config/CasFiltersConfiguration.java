package org.jasig.cas.config;

import com.google.common.collect.ImmutableSet;
import org.jasig.cas.security.RequestParameterPolicyEnforcementFilter;
import org.jasig.cas.security.ResponseHeadersEnforcementFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.CharacterEncodingFilter;

/**
 * This is {@link CasFiltersConfiguration} that attempts to create Spring-managed beans
 * backed by external configuration.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("casFiltersConfiguration")
public class CasFiltersConfiguration {

    @Value("${httprequest.web.encoding:UTF-8}")
    private String encoding;

    @Value("${httprequest.web.encoding.force:true}")
    private boolean forceEncoding;

    @Value("${httpresponse.header.cache:false}")
    private boolean headerCache;

    @Value("${httpresponse.header.hsts:false}")
    private boolean headerHsts;

    @Value("${httpresponse.header.xframe:false}")
    private boolean headerXframe;

    @Value("${httpresponse.header.xcontent:false}")
    private boolean headerXcontent;

    @Value("${httpresponse.header.xss:false}")
    private boolean headerXss;

    @Value("${cas.http.allow.multivalue.params:false}")
    private boolean allowMultiValueParameters;

    @Value("${cas.http.allow.post.params:username,password}")
    private String onlyPostParams;

    @Value("${cas.http.check.params:"
            + " ticket,service,renew,gateway,warn,method,target,SAMLart,pgtUrl,pgt,pgtId,pgtIou,targetService,entityId}")
    private String paramsToCheck;

    @Bean(name = "characterEncodingFilter")
    public CharacterEncodingFilter characterEncodingFilter() {
        return new CharacterEncodingFilter(this.encoding, this.forceEncoding);
    }

    @Bean(name = "responseHeadersSecurityFilter")
    public ResponseHeadersEnforcementFilter rsponseHeadersSecurityFilter() {
        final ResponseHeadersEnforcementFilter filter = new ResponseHeadersEnforcementFilter();
        filter.setEnableCacheControl(this.headerCache);
        filter.setEnableStrictTransportSecurity(this.headerHsts);
        filter.setEnableXFrameOptions(this.headerXframe);
        filter.setEnableXContentTypeOptions(this.headerXcontent);
        filter.setEnableXSSProtection(this.headerXss);
        return filter;
    }

    @Bean(name = "requestParameterSecurityFilter")
    public RequestParameterPolicyEnforcementFilter requestParameterSecurityFilter() {
        final RequestParameterPolicyEnforcementFilter filter = new RequestParameterPolicyEnforcementFilter();
        filter.setAllowMultiValueParameters(this.allowMultiValueParameters);
        filter.setParametersToCheck(StringUtils.commaDelimitedListToSet(this.paramsToCheck));
        filter.setCharactersToForbid(ImmutableSet.of());
        filter.setOnlyPostParameters(StringUtils.commaDelimitedListToSet(this.onlyPostParams));
        return filter;
    }
}
