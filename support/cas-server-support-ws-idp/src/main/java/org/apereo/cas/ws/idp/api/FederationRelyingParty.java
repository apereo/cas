package org.apereo.cas.ws.idp.api;

import org.apereo.cas.ws.idp.DefaultFederationClaim;

import java.util.Collection;

/**
 * This is {@link FederationRelyingParty}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface FederationRelyingParty {
    String getRealm();

    String getProtocol();

    String getDisplayName();

    String getDescription();

    String getTokenType();

    long getLifetime();
    
    String getRole();

    Collection<DefaultFederationClaim> getClaims();
}
