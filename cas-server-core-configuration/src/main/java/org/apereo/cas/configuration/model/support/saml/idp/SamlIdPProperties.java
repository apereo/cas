package org.apereo.cas.configuration.model.support.saml.idp;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

/**
 * This is {@link SamlIdPProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class SamlIdPProperties {
    
    private String entityId;
    private String hostName;
    private String scope;
    
    private Response response = new Response();
    private Metadata metadata = new Metadata();
    private Logout logout = new Logout();

    public Logout getLogout() {
        return logout;
    }

    public void setLogout(final Logout logout) {
        this.logout = logout;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(final Response response) {
        this.response = response;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(final String entityId) {
        this.entityId = entityId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(final String hostName) {
        this.hostName = hostName;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(final String scope) {
        this.scope = scope;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(final Metadata metadata) {
        this.metadata = metadata;
    }

    public static class Metadata {
        private boolean failFast = true;
        private boolean requireValidMetadata = true;
        private long cacheExpirationMinutes = 30;
        private File location;
        private String privateKeyAlgName;
        
        public boolean isFailFast() {
            return failFast;
        }

        public void setFailFast(final boolean failFast) {
            this.failFast = failFast;
        }

        public boolean isRequireValidMetadata() {
            return requireValidMetadata;
        }

        public void setRequireValidMetadata(final boolean requireValidMetadata) {
            this.requireValidMetadata = requireValidMetadata;
        }

        public long getCacheExpirationMinutes() {
            return cacheExpirationMinutes;
        }

        public void setCacheExpirationMinutes(final long cacheExpirationMinutes) {
            this.cacheExpirationMinutes = cacheExpirationMinutes;
        }

        public File getLocation() {
            return location;
        }

        public void setLocation(final File location) {
            this.location = location;
        }
        
        public File getSigningCertFile() {
            return new File(getLocation(), "/idp-signing.crt");
        }

        public File getSigningKeyFile() {
            return new File(getLocation(), "/idp-signing.key");
        }

        public String getPrivateKeyAlgName() {
            return privateKeyAlgName;
        }

        public void setPrivateKeyAlgName(final String privateKeyAlgName) {
            this.privateKeyAlgName = privateKeyAlgName;
        }


        public File getEncryptionCertFile() {
            return new File(getLocation(), "/idp-encryption.crt");
        }

        public File getEncryptionKeyFile() {
            return new File(getLocation(), "/idp-encryption.key");
        }
        
    }
    
    public static class Response {
        private int skewAllowance;
        private String overrideSignatureCanonicalizationAlgorithm;
        private boolean signError;
        private boolean useAttributeFriendlyName = true;

        public boolean isUseAttributeFriendlyName() {
            return useAttributeFriendlyName;
        }

        public void setUseAttributeFriendlyName(final boolean useAttributeFriendlyName) {
            this.useAttributeFriendlyName = useAttributeFriendlyName;
        }

        public int getSkewAllowance() {
            return skewAllowance;
        }

        public void setSkewAllowance(final int skewAllowance) {
            this.skewAllowance = skewAllowance;
        }

        public String getOverrideSignatureCanonicalizationAlgorithm() {
            return overrideSignatureCanonicalizationAlgorithm;
        }

        public void setOverrideSignatureCanonicalizationAlgorithm(final String overrideSignatureCanonicalizationAlgorithm) {
            this.overrideSignatureCanonicalizationAlgorithm = overrideSignatureCanonicalizationAlgorithm;
        }

        public boolean isSignError() {
            return signError;
        }

        public void setSignError(final boolean signError) {
            this.signError = signError;
        }
    }
    
    public static class Logout {
        private boolean forceSignedLogoutRequests = true;
        private boolean singleLogoutCallbacksDisabled;

        public boolean isForceSignedLogoutRequests() {
            return forceSignedLogoutRequests;
        }

        public void setForceSignedLogoutRequests(final boolean forceSignedLogoutRequests) {
            this.forceSignedLogoutRequests = forceSignedLogoutRequests;
        }

        public boolean isSingleLogoutCallbacksDisabled() {
            return singleLogoutCallbacksDisabled;
        }

        public void setSingleLogoutCallbacksDisabled(final boolean singleLogoutCallbacksDisabled) {
            this.singleLogoutCallbacksDisabled = singleLogoutCallbacksDisabled;
        }
    }
}
