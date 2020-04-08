package org.apereo.cas.support.openid.web.support;

import org.apereo.cas.support.openid.OpenIdProtocolConstants;

import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * OpenID url handling mappings.
 *
 * @author Scott Battaglia
 * @deprecated 6.2
 * @since 3.1
 */
@Deprecated(since = "6.2.0")
public class OpenIdPostUrlHandlerMapping extends SimpleUrlHandlerMapping {

    @Override
    protected Object lookupHandler(final String urlPath, final HttpServletRequest request) throws Exception {
        if (HttpMethod.POST.name().equals(request.getMethod())
            && (OpenIdProtocolConstants.CHECK_AUTHENTICATION
            .equals(request.getParameter(OpenIdProtocolConstants.OPENID_MODE))
            || OpenIdProtocolConstants.ASSOCIATE
            .equals(request.getParameter(OpenIdProtocolConstants.OPENID_MODE)))) {
            return super.lookupHandler(urlPath, request);
        }

        return null;
    }
}
