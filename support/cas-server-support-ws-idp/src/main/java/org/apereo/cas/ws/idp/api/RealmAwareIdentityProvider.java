package org.apereo.cas.ws.idp.api;

import org.apache.cxf.fediz.core.Claim;
import org.springframework.core.io.Resource;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This is {@link RealmAwareIdentityProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface RealmAwareIdentityProvider {

    Resource getCertificate();
    
    String getCertificatePassword();
    
    String getRealm();

    String getUri();

    URL getStsUrl();

    URL getIdpUrl();

    Collection<String> getSupportedProtocols();

    Collection<Claim> getClaimTypesOffered();
    
    Map<String, FederationRelyingParty> getRelyingParties();

    Map<String, String> getAuthenticationURIs();

    String getDisplayName();

    String getDescription();
}
