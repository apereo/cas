package org.jasig.cas.support.oauth;

import org.jasig.cas.authentication.principal.AbstractWebApplicationService;

/**
 * OAuth web application service.
 *
 * @author Jerome Leleu
 * @since 4.2.0
 */
public final class OAuthWebApplicationService extends AbstractWebApplicationService {

    /**
     * Instantiates a new OAuth web application service impl.
     *
     * @param id the id
     */
    public OAuthWebApplicationService(final long id) {
        super("" + id, null, null, null);
    }
}
