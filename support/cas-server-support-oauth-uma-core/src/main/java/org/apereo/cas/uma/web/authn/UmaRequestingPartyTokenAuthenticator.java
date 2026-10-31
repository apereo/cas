package org.apereo.cas.uma.web.authn;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20Constants;

/**
 * This is {@link UmaRequestingPartyTokenAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class UmaRequestingPartyTokenAuthenticator extends BaseUmaTokenAuthenticator {
    @Override
    protected String getRequiredScope() {
        return OAuth20Constants.UMA_PROTECTION_SCOPE;
    }
}
