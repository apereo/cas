package org.apereo.cas.configuration.model.support.wsfed;

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
        private String realm = "urn:org:apereo:cas:ws:idp:realm-CAS";
        private String realmName = "CAS";
        
        public String getRealm() {
            return realm;
        }

        public void setRealm(final String realm) {
            this.realm = realm;
        }

        public String getRealmName() {
            return realmName;
        }

        public void setRealmName(final String realmName) {
            this.realmName = realmName;
        }
    }
    
    public static class SecurityTokenService {
        private String subjectNameIdFormat = "unspecified";
        private boolean encryptTokens = true;
        
        private String signingKeystoreFile;
        private String signingKeystorePassword;

        private String encryptionKeystoreFile;
        private String encryptionKeystorePassword;

        private String encryptionKey;
        private String signingKey;
        
        private RealmDefinition realm = new RealmDefinition();

        public String getEncryptionKey() {
            return encryptionKey;
        }

        public void setEncryptionKey(final String encryptionKey) {
            this.encryptionKey = encryptionKey;
        }

        public String getSigningKey() {
            return signingKey;
        }

        public void setSigningKey(final String signingKey) {
            this.signingKey = signingKey;
        }

        public RealmDefinition getRealm() {
            return realm;
        }

        public void setRealm(final RealmDefinition realm) {
            this.realm = realm;
        }

        public boolean isEncryptTokens() {
            return encryptTokens;
        }

        public void setEncryptTokens(final boolean encryptTokens) {
            this.encryptTokens = encryptTokens;
        }

        public String getSubjectNameIdFormat() {
            return subjectNameIdFormat;
        }

        public void setSubjectNameIdFormat(final String subjectNameIdFormat) {
            this.subjectNameIdFormat = subjectNameIdFormat;
        }

        public String getSigningKeystoreFile() {
            return signingKeystoreFile;
        }

        public void setSigningKeystoreFile(final String signingKeystoreFile) {
            this.signingKeystoreFile = signingKeystoreFile;
        }

        public String getSigningKeystorePassword() {
            return signingKeystorePassword;
        }

        public void setSigningKeystorePassword(final String signingKeystorePassword) {
            this.signingKeystorePassword = signingKeystorePassword;
        }

        public String getEncryptionKeystoreFile() {
            return encryptionKeystoreFile;
        }

        public void setEncryptionKeystoreFile(final String encryptionKeystoreFile) {
            this.encryptionKeystoreFile = encryptionKeystoreFile;
        }

        public String getEncryptionKeystorePassword() {
            return encryptionKeystorePassword;
        }

        public void setEncryptionKeystorePassword(final String encryptionKeystorePassword) {
            this.encryptionKeystorePassword = encryptionKeystorePassword;
        }
        
        public static class RealmDefinition {
            private String keystoreFile;
            private String keystorePassword;
            private String keystoreAlias;
            private String keyPassword;
            private String issuer = "CAS";

            public String getKeyPassword() {
                return keyPassword;
            }

            public void setKeyPassword(final String keyPassword) {
                this.keyPassword = keyPassword;
            }

            public String getKeystoreFile() {
                return keystoreFile;
            }

            public void setKeystoreFile(final String keystoreFile) {
                this.keystoreFile = keystoreFile;
            }

            public String getKeystorePassword() {
                return keystorePassword;
            }

            public void setKeystorePassword(final String keystorePassword) {
                this.keystorePassword = keystorePassword;
            }

            public String getKeystoreAlias() {
                return keystoreAlias;
            }

            public void setKeystoreAlias(final String keystoreAlias) {
                this.keystoreAlias = keystoreAlias;
            }

            public String getIssuer() {
                return issuer;
            }

            public void setIssuer(final String issuer) {
                this.issuer = issuer;
            }
        }
    }
}
