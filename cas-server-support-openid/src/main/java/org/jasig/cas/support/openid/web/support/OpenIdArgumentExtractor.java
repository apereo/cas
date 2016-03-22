package org.jasig.cas.support.openid.web.support;

import org.apache.commons.lang3.NotImplementedException;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.web.support.AbstractArgumentExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;

/**
 * @deprecated As of 4.2, use {@link org.jasig.cas.web.support.DefaultArgumentExtractor}.
 * Constructs an OpenId Service.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Deprecated
@RefreshScope
@Component("openIdArgumentExtractor")
public class OpenIdArgumentExtractor extends AbstractArgumentExtractor {
    /**
     * The prefix url for OpenID (without the trailing slash).
     */
    
    @Value("${server.prefix}/openid")
    private String openIdPrefixUrl;

    @Override
    protected WebApplicationService extractServiceInternal(final HttpServletRequest request) {
        throw new NotImplementedException("This operation is not supported. "
                + "The class is deprecated and will be removed in future versions");
    }

    public String getOpenIdPrefixUrl() {
        return openIdPrefixUrl;
    }

    public void setOpenIdPrefixUrl(final String openIdPrefixUrl) {
        this.openIdPrefixUrl = openIdPrefixUrl;
    }
}
