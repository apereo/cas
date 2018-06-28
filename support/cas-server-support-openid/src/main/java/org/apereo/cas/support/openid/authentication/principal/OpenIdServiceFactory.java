package org.apereo.cas.support.openid.authentication.principal;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public class OpenIdServiceFactory extends AbstractServiceFactory<OpenIdService> {

    private final String openIdPrefixUrl;

    @Override
    public OpenIdService createService(final HttpServletRequest request) {
        final var service = request.getParameter(OpenIdProtocolConstants.OPENID_RETURNTO);
        final var openIdIdentity = request.getParameter(OpenIdProtocolConstants.OPENID_IDENTITY);

        if (openIdIdentity == null || !StringUtils.hasText(service)) {
            return null;
        }

        final var id = cleanupUrl(service);
        final var artifactId = request.getParameter(OpenIdProtocolConstants.OPENID_ASSOCHANDLE);
        final var s = new OpenIdService(id, service, artifactId, openIdIdentity);
        s.setLoggedOutAlready(true);
        s.setSource(OpenIdProtocolConstants.OPENID_RETURNTO);
        return s;
    }

    @Override
    public OpenIdService createService(final String id) {
        return new OpenIdService(id, id, null, this.openIdPrefixUrl);
    }
}
