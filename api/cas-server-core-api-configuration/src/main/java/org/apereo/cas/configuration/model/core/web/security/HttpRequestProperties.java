package org.apereo.cas.configuration.model.core.web.security;

import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;

/**
 * This is {@link HttpRequestProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
public class HttpRequestProperties implements Serializable {

    private static final long serialVersionUID = -5175966163542099866L;
    /**
     * Whether CAS should accept multi-valued parameters
     * in incoming requests. Example block would to prevent
     * requests where more than one {@code service} parameter is specified.
     */
    private boolean allowMultiValueParameters;
    /**
     * Parameters that are only allowed and accepted during posts.
     */
    private String onlyPostParams = "username,password";

    /**
     * Parameters to sanitize and cross-check in incoming requests.
     * The special value * instructs the Filter to check all parameters.
     */
    private String paramsToCheck =
        "ticket,service,renew,gateway,warn,method,target,SAMLart,"
            + "pgtUrl,pgt,pgtId,pgtIou,targetService,entityId,token";

    /**
     * Control http request settings.
     */
    @NestedConfigurationProperty
    private HttpWebRequestProperties web = new HttpWebRequestProperties();
    /**
     * Enforce request header options and security settings.
     */
    @NestedConfigurationProperty
    private HttpHeadersRequestProperties header = new HttpHeadersRequestProperties();
    /**
     * Control CORS settings for requests.
     */
    @NestedConfigurationProperty
    private HttpCorsRequestProperties cors = new HttpCorsRequestProperties();
    
    public boolean isAllowMultiValueParameters() {
        return allowMultiValueParameters;
    }

    public void setAllowMultiValueParameters(final boolean allowMultiValueParameters) {
        this.allowMultiValueParameters = allowMultiValueParameters;
    }

    public String getOnlyPostParams() {
        return onlyPostParams;
    }

    public void setOnlyPostParams(final String onlyPostParams) {
        this.onlyPostParams = onlyPostParams;
    }

    public String getParamsToCheck() {
        return paramsToCheck;
    }

    public void setParamsToCheck(final String paramsToCheck) {
        this.paramsToCheck = paramsToCheck;
    }

    public HttpCorsRequestProperties getCors() {
        return cors;
    }

    public void setCors(final HttpCorsRequestProperties cors) {
        this.cors = cors;
    }

    public HttpWebRequestProperties getWeb() {
        return web;
    }

    public void setWeb(final HttpWebRequestProperties web) {
        this.web = web;
    }

    public HttpHeadersRequestProperties getHeader() {
        return header;
    }

    public void setHeader(final HttpHeadersRequestProperties header) {
        this.header = header;
    }

}
