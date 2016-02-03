package org.jasig.cas.support.oauth.services;

import org.jasig.cas.authentication.principal.AbstractWebApplicationService;

/**
 * OAuth web application service.
 *
 * @author Jerome Leleu
 * @since 4.3.0
 */
public final class OAuthWebApplicationService extends AbstractWebApplicationService {

    /**
     * Instantiates a new OAuth web application service impl.
     *
     * @param id the id
     * @param originalUrl the original url
     */
    public OAuthWebApplicationService(final String id, final String originalUrl) {
        super(id, originalUrl, null, null);
    }
}
