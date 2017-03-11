package org.apereo.cas.configuration.model.support.wsfed;

import java.io.File;

/**
 * This is {@link WsFederationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class WsFederationProperties {
    private IdentityProvider idp = new IdentityProvider();
    private SecurityTokenService sts = new SecurityTokenService();

    public IdentityProvider getIdp() {
        return idp;
    }

    public void setIdp(final IdentityProvider idp) {
        this.idp = idp;
    }

    public SecurityTokenService getSts() {
        return sts;
    }

    public void setSts(final SecurityTokenService sts) {
        this.sts = sts;
    }

    public static class IdentityProvider {
        private String realm;
        private String uri;
        private File certificate;
        private String certificatePassword;

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

        public String getRealm() {
            return realm;
        }

        public void setRealm(final String realm) {
            this.realm = realm;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(final String uri) {
            this.uri = uri;
        }
    }
    
    public static class SecurityTokenService {
        
    }
}
