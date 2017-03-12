package org.apereo.cas.ws.idp;

import org.apache.cxf.fediz.core.Claim;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

/**
 * This is {@link RealmAwareIdentityProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface RealmAwareIdentityProvider {

    File getCertificate();
    
    String getCertificatePassword();
    
    String getRealm();

    String getUri();

    URL getStsUrl();

    URL getIdpUrl();

    Collection<String> getSupportedProtocols();

    Collection<Claim> getClaimTypesOffered();
    
    Map<String, String> getAuthenticationURIs();

    String getDisplayName();

    String getDescription();
}
