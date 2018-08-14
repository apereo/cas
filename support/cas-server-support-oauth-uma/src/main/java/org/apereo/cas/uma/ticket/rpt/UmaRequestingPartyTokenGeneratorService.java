package org.apereo.cas.uma.ticket.rpt;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.BaseIdTokenSigningAndEncryptionService;

import org.jose4j.jwt.JwtClaims;

/**
 * This is {@link UmaRequestingPartyTokenGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class UmaRequestingPartyTokenGeneratorService extends BaseIdTokenSigningAndEncryptionService {
    @Override
    public String encode(final OAuthRegisteredService svc, final JwtClaims claims) {
        return null;
    }
}
