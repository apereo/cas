package org.apereo.cas.support.openid.authentication.principal;

import org.apereo.cas.authentication.principal.AbstractServiceFactory;
import org.apereo.cas.support.openid.OpenIdProtocolConstants;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * The {@link OpenIdServiceFactory} creates {@link OpenIdService} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class OpenIdServiceFactory extends AbstractServiceFactory<OpenIdService> {

    private final String openIdPrefixUrl;

    public OpenIdServiceFactory(final String openIdPrefixUrl) {
        this.openIdPrefixUrl = openIdPrefixUrl;
    }

    @Override
    public OpenIdService createService(final HttpServletRequest request) {
        final String service = request.getParameter(OpenIdProtocolConstants.OPENID_RETURNTO);
        final String openIdIdentity = request.getParameter(OpenIdProtocolConstants.OPENID_IDENTITY);

        if (openIdIdentity == null || !StringUtils.hasText(service)) {
            return null;
        }

        final String id = cleanupUrl(service);
        final String artifactId = request.getParameter(OpenIdProtocolConstants.OPENID_ASSOCHANDLE);
        final OpenIdService s = new OpenIdService(id, service, artifactId, openIdIdentity);
        s.setLoggedOutAlready(true);
        return s;
    }

    @Override
    public OpenIdService createService(final String id) {
        return new OpenIdService(id, id, null, this.openIdPrefixUrl);
    }
}
