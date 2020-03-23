package org.apereo.cas.configuration.model.core.web.security;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link HttpRequestProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
@Getter
@Setter
@Accessors(chain = true)
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
    private String paramsToCheck = "ticket,service,renew,gateway,warn,method,target,SAMLart," + "pgtUrl,pgt,pgtId,pgtIou,targetService,entityId,token";

    /**
     * Characters to block in incoming requests.
     * {@code none} is a special value. Separate characters by a space.
     */
    private String charactersToForbid = "none";

    /**
     * Specify a regular expression that would be checked
     * against the request URL. If a successful match is found,
     * the request would be blocked.
     */
    private String patternToBlock;

    /**
     * Custom response headers to inject into the response as needed.
     */
    private Map<String, String> customHeaders = new LinkedHashMap<>(0);

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
}
