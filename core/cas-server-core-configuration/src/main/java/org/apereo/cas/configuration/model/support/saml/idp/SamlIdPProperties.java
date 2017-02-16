package org.apereo.cas.configuration.model.support.saml.idp;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link SamlIdPProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class SamlIdPProperties {

    private String entityId = "https://cas.example.org/idp";
    private String scope = "example.org";
    private Set<String> authenticationContextClassMappings;

    private Response response = new Response();
    private Metadata metadata = new Metadata();
    private Logout logout = new Logout();
    private Algorithms algs = new Algorithms();

    public Set<String> getAuthenticationContextClassMappings() {
        return authenticationContextClassMappings;
    }

    public void setAuthenticationContextClassMappings(final Set<String> authenticationContextClassMappings) {
        this.authenticationContextClassMappings = authenticationContextClassMappings;
    }

    public Algorithms getAlgs() {
        return algs;
    }

    public void setAlgs(final Algorithms algs) {
        this.algs = algs;
    }

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
        private long cacheExpirationMinutes = TimeUnit.DAYS.toMinutes(1);
        private Resource location = new FileSystemResource("/etc/cas/saml");
        private String privateKeyAlgName = "RSA";
        private String basicAuthnUsername;
        private String basicAuthnPassword;
        private List<String> supportedContentTypes = new ArrayList<>();

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

        public Resource getLocation() {
            return location;
        }

        public void setLocation(final Resource location) {
            this.location = location;
        }

        /**
         * Gets signing cert file.
         *
         * @return the signing cert file
         * @throws Exception the exception
         */
        public Resource getSigningCertFile() throws Exception {
            return new FileSystemResource(new File(this.location.getFile(), "/idp-signing.crt"));
        }

        /**
         * Gets signing key file.
         *
         * @return the signing key file
         * @throws Exception the exception
         */
        public Resource getSigningKeyFile() throws Exception {
            return new FileSystemResource(new File(this.location.getFile(), "/idp-signing.key"));
        }

        public String getPrivateKeyAlgName() {
            return privateKeyAlgName;
        }

        public void setPrivateKeyAlgName(final String privateKeyAlgName) {
            this.privateKeyAlgName = privateKeyAlgName;
        }

        /**
         * Gets encryption cert file.
         *
         * @return the encryption cert file
         * @throws Exception the exception
         */
        public Resource getEncryptionCertFile() throws Exception {
            return new FileSystemResource(new File(this.location.getFile(), "/idp-encryption.crt"));
        }

        /**
         * Gets encryption key file.
         *
         * @return the encryption key file
         * @throws Exception the exception
         */
        public Resource getEncryptionKeyFile() throws Exception {
            return new FileSystemResource(new File(this.location.getFile(), "/idp-encryption.key"));
        }

        /**
         * Gets metadata file.
         *
         * @return the metadata file
         * @throws Exception the exception
         */
        public File getMetadataFile() throws Exception {
            return new File(this.location.getFile(), "idp-metadata.xml");
        }

        public String getBasicAuthnUsername() {
            return basicAuthnUsername;
        }

        public void setBasicAuthnUsername(final String basicAuthnUsername) {
            this.basicAuthnUsername = basicAuthnUsername;
        }

        public String getBasicAuthnPassword() {
            return basicAuthnPassword;
        }

        public void setBasicAuthnPassword(final String basicAuthnPassword) {
            this.basicAuthnPassword = basicAuthnPassword;
        }

        public List<String> getSupportedContentTypes() {
            return supportedContentTypes;
        }

        public void setSupportedContentTypes(final List<String> supportedContentTypes) {
            this.supportedContentTypes = supportedContentTypes;
        }
    }

    public static class Response {
        private int skewAllowance;
        private boolean signError;
        private boolean useAttributeFriendlyName = true;
        private List<String> attributeNameFormats = new ArrayList<>();

        public List<String> getAttributeNameFormats() {
            return attributeNameFormats;
        }

        public void setAttributeNameFormats(final List<String> attributeNameFormats) {
            this.attributeNameFormats = attributeNameFormats;
        }

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

        public boolean isSignError() {
            return signError;
        }

        public void setSignError(final boolean signError) {
            this.signError = signError;
        }

        /**
         * Configure attribute name formats and build a map.
         *
         * @return the map
         */
        public Map<String, String> configureAttributeNameFormats() {
            if (this.attributeNameFormats.isEmpty()) {
                return Collections.emptyMap();
            }
            final Map<String, String> nameFormats = new HashMap<>();
            this.attributeNameFormats.forEach(value -> Arrays.stream(value.split(",")).forEach(format -> {
                final String[] values = format.split("->");
                if (values.length == 2) {
                    nameFormats.put(values[0], values[1]);
                }
            }));
            return nameFormats;
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

    public static class Algorithms {
        private List overrideDataEncryptionAlgorithms;
        private List overrideKeyEncryptionAlgorithms;
        private List overrideBlackListedEncryptionAlgorithms;
        private List overrideWhiteListedAlgorithms;
        private List overrideSignatureReferenceDigestMethods;
        private List overrideSignatureAlgorithms;
        private List overrideBlackListedSignatureSigningAlgorithms;
        private List overrideWhiteListedSignatureSigningAlgorithms;
        private String overrideSignatureCanonicalizationAlgorithm;

        public String getOverrideSignatureCanonicalizationAlgorithm() {
            return overrideSignatureCanonicalizationAlgorithm;
        }

        public void setOverrideSignatureCanonicalizationAlgorithm(final String overrideSignatureCanonicalizationAlgorithm) {
            this.overrideSignatureCanonicalizationAlgorithm = overrideSignatureCanonicalizationAlgorithm;
        }

        public List getOverrideDataEncryptionAlgorithms() {
            return overrideDataEncryptionAlgorithms;
        }

        public void setOverrideDataEncryptionAlgorithms(final List overrideDataEncryptionAlgorithms) {
            this.overrideDataEncryptionAlgorithms = overrideDataEncryptionAlgorithms;
        }

        public List getOverrideKeyEncryptionAlgorithms() {
            return overrideKeyEncryptionAlgorithms;
        }

        public void setOverrideKeyEncryptionAlgorithms(final List overrideKeyEncryptionAlgorithms) {
            this.overrideKeyEncryptionAlgorithms = overrideKeyEncryptionAlgorithms;
        }

        public List getOverrideBlackListedEncryptionAlgorithms() {
            return overrideBlackListedEncryptionAlgorithms;
        }

        public void setOverrideBlackListedEncryptionAlgorithms(final List overrideBlackListedEncryptionAlgorithms) {
            this.overrideBlackListedEncryptionAlgorithms = overrideBlackListedEncryptionAlgorithms;
        }

        public List getOverrideWhiteListedAlgorithms() {
            return overrideWhiteListedAlgorithms;
        }

        public void setOverrideWhiteListedAlgorithms(final List overrideWhiteListedAlgorithms) {
            this.overrideWhiteListedAlgorithms = overrideWhiteListedAlgorithms;
        }

        public List getOverrideSignatureReferenceDigestMethods() {
            return overrideSignatureReferenceDigestMethods;
        }

        public void setOverrideSignatureReferenceDigestMethods(final List overrideSignatureReferenceDigestMethods) {
            this.overrideSignatureReferenceDigestMethods = overrideSignatureReferenceDigestMethods;
        }

        public List getOverrideSignatureAlgorithms() {
            return overrideSignatureAlgorithms;
        }

        public void setOverrideSignatureAlgorithms(final List overrideSignatureAlgorithms) {
            this.overrideSignatureAlgorithms = overrideSignatureAlgorithms;
        }

        public List getOverrideBlackListedSignatureSigningAlgorithms() {
            return overrideBlackListedSignatureSigningAlgorithms;
        }

        public void setOverrideBlackListedSignatureSigningAlgorithms(final List overrideBlackListedSignatureSigningAlgorithms) {
            this.overrideBlackListedSignatureSigningAlgorithms = overrideBlackListedSignatureSigningAlgorithms;
        }

        public List getOverrideWhiteListedSignatureSigningAlgorithms() {
            return overrideWhiteListedSignatureSigningAlgorithms;
        }

        public void setOverrideWhiteListedSignatureSigningAlgorithms(final List overrideWhiteListedSignatureSigningAlgorithms) {
            this.overrideWhiteListedSignatureSigningAlgorithms = overrideWhiteListedSignatureSigningAlgorithms;
        }
    }
}
