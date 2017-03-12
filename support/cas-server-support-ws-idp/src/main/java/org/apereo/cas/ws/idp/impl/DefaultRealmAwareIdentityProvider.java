package org.apereo.cas.ws.idp.impl;

import org.apache.cxf.fediz.core.Claim;
import org.apereo.cas.ws.idp.RealmAwareIdentityProvider;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link DefaultRealmAwareIdentityProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultRealmAwareIdentityProvider implements RealmAwareIdentityProvider {
    private String realm;
    private String uri;
    private URL stsUrl;
    private URL idpUrl;

    private File certificate;
    private String certificatePassword;

    private Collection<Claim> claimTypesOffered = new ArrayList<>();
    private Collection<String> supportedProtocols = new ArrayList<>();
    private Map<String, String> authenticationURIs = new HashMap<>();
    private String displayName;
    private String description;

    @Override
    public File getCertificate() {
        return certificate;
    }

    public void setCertificate(final File certificate) {
        this.certificate = certificate;
    }

    public String getCertificatePassword() {
        return certificatePassword;
    }

    public void setCertificatePassword(final String certificatePassword) {
        this.certificatePassword = certificatePassword;
    }

    @Override
    public String getRealm() {
        return realm;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public URL getStsUrl() {
        return stsUrl;
    }

    @Override
    public URL getIdpUrl() {
        return idpUrl;
    }

    @Override
    public Collection<String> getSupportedProtocols() {
        return supportedProtocols;
    }

    @Override
    public Map<String, String> getAuthenticationURIs() {
        return authenticationURIs;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Collection<Claim> getClaimTypesOffered() {
        return claimTypesOffered;
    }

    public void setClaimTypesOffered(final Collection<Claim> claimTypesOffered) {
        this.claimTypesOffered = claimTypesOffered;
    }

    public void setRealm(final String realm) {
        this.realm = realm;
    }

    public void setUri(final String uri) {
        this.uri = uri;
    }

    public void setStsUrl(final URL stsUrl) {
        this.stsUrl = stsUrl;
    }

    public void setIdpUrl(final URL idpUrl) {
        this.idpUrl = idpUrl;
    }

    public void setSupportedProtocols(final Collection<String> supportedProtocols) {
        this.supportedProtocols = supportedProtocols;
    }

    public void setAuthenticationURIs(final Map<String, String> authenticationURIs) {
        this.authenticationURIs = authenticationURIs;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
