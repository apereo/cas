package org.apereo.cas.configuration.model.support.saml.idp;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link SamlIdPProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-saml-idp")
public class SamlIdPProperties implements Serializable {

    private static final long serialVersionUID = -5848075783676789852L;

    /**
     * Indicates whether attribute query profile is enabled.
     * Enabling this setting would allow CAS to record SAML
     * responses and have them be made available later for attribute lookups.
     */
    private boolean attributeQueryProfileEnabled;

    /**
     * The SAML entity id for the deployment.
     */
    @RequiredProperty
    private String entityId = "https://cas.example.org/idp";
    /**
     * The scope used in generation of metadata.
     */
    @RequiredProperty
    private String scope = "example.org";
    /**
     * A mapping of authentication context class refs.
     * This is where specific authentication context classes
     * are references and mapped one ones that CAS may support
     * mainly for MFA purposes.
     * <p>
     * Example might be {@code urn:oasis:names:tc:SAML:2.0:ac:classes:SomeClassName->mfa-duo}.
     */
    private List<String> authenticationContextClassMappings;

    /**
     * Settings related to SAML2 responses.
     */
    private Response response = new Response();
    /**
     * SAML2 metadata related settings.
     */
    private Metadata metadata = new Metadata();
    /**
     * SAML2 logout related settings.
     */
    private Logout logout = new Logout();
    /**
     * Settings related to algorithms used for signing, etc.
     */
    private Algorithms algs = new Algorithms();

    public boolean isAttributeQueryProfileEnabled() {
        return attributeQueryProfileEnabled;
    }

    public void setAttributeQueryProfileEnabled(final boolean attributeQueryProfileEnabled) {
        this.attributeQueryProfileEnabled = attributeQueryProfileEnabled;
    }

    public List<String> getAuthenticationContextClassMappings() {
        return authenticationContextClassMappings;
    }

    public void setAuthenticationContextClassMappings(final List<String> authenticationContextClassMappings) {
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

    public static class Metadata implements Serializable {
        private static final long serialVersionUID = -1020542741768471305L;
        /**
         * Whether invalid metadata should eagerly fail quickly on startup
         * once the resource is parsed.
         */
        private boolean failFast = true;
        /**
         * Whether valid metadata is required.
         */
        private boolean requireValidMetadata = true;
        /**
         * How long should metadata be cached in minutes.
         */
        private long cacheExpirationMinutes = TimeUnit.DAYS.toMinutes(1);
        /**
         * Directory location of SAML metadata and signing/encryption keys.
         * This directory will be used to hold the configuration files.
         */
        @RequiredProperty
        private Resource location = new FileSystemResource("/etc/cas/saml");

        /**
         * Algorithm name to use when generating private key.
         */
        private String privateKeyAlgName = "RSA";

        /**
         * Basic auth username in case the metadata instance is connecting to an MDQ server.
         */
        private String basicAuthnUsername;
        /**
         * Basic auth password in case the metadata instance is connecting to an MDQ server.
         */
        private String basicAuthnPassword;
        /**
         * Supported content types in case the metadata instance is connecting to an MDQ server.
         */
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
         * Gets full location of signing cert file.
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
         * Gets idp metadata file.
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

    public static class Response implements Serializable {
        private static final long serialVersionUID = 7200477683583467619L;

        /**
         * Indicate the type of encoding used when rendering the
         * saml response and its signature blog.
         */
        public enum SignatureCredentialTypes {
            /**
             * DER-Encoded format.
             */
            BASIC,
            /**
             * PEM-encoded X509 format.
             */
            X509
        }

        /**
         * Indicate the encoding type of the credential used when rendering the saml response.
         */
        private SignatureCredentialTypes credentialType = SignatureCredentialTypes.X509;
        
        /**
         * Time unit in seconds used to skew authentication dates such
         * as valid-from and valid-until elements.
         */
        private int skewAllowance = 5;
        /**
         * Whether error responses should be signed.
         */
        private boolean signError;
        /**
         * The default authentication context class to include in the response
         * if none is specified via the service.
         */
        private String defaultAuthenticationContextClass = "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport";
        /**
         * Indicates the default name-format for all attributes
         * in case the individual attribute is not individually mapped.
         */
        private String defaultAttributeNameFormat = "uri";
        /**
         * When creating attribute definitions, will ensure
         * the attribute's friendly name is set to the actual attribute name
         * in the event that the service provider wishes to use the friendly name.
         */
        private boolean useAttributeFriendlyName = true;
        /**
         * Each individual attribute can be mapped to a particular name-format.
         * Example: {@code attributeName->basic|uri|unspecified|custom-format-etc,...}.
         */
        private List<String> attributeNameFormats = new ArrayList<>();

        public SignatureCredentialTypes getCredentialType() {
            return credentialType;
        }

        public void setCredentialType(final SignatureCredentialTypes credentialType) {
            this.credentialType = credentialType;
        }

        public String getDefaultAuthenticationContextClass() {
            return defaultAuthenticationContextClass;
        }

        public void setDefaultAuthenticationContextClass(final String defaultAuthenticationContextClass) {
            this.defaultAuthenticationContextClass = defaultAuthenticationContextClass;
        }

        public String getDefaultAttributeNameFormat() {
            return defaultAttributeNameFormat;
        }

        public void setDefaultAttributeNameFormat(final String defaultAttributeNameFormat) {
            this.defaultAttributeNameFormat = defaultAttributeNameFormat;
        }

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
                return new HashMap<>(0);
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

    public static class Logout implements Serializable {
        private static final long serialVersionUID = -4608824149569614549L;

        /**
         * Whether SLO logout requests are required to be signed.
         */
        private boolean forceSignedLogoutRequests = true;
        /**
         * Whether SAML SLO is enabled and processed.
         */
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

    public static class Algorithms implements Serializable {
        private static final long serialVersionUID = 6547093517788229284L;
        /**
         * The Override data encryption algorithms.
         */
        private List overrideDataEncryptionAlgorithms;
        /**
         * The Override key encryption algorithms.
         */
        private List overrideKeyEncryptionAlgorithms;
        /**
         * The Override black listed encryption algorithms.
         */
        private List overrideBlackListedEncryptionAlgorithms;
        /**
         * The Override white listed algorithms.
         */
        private List overrideWhiteListedAlgorithms;
        /**
         * The Override signature reference digest methods.
         */
        private List overrideSignatureReferenceDigestMethods;
        /**
         * The Override signature algorithms.
         */
        private List overrideSignatureAlgorithms;
        /**
         * The Override black listed signature signing algorithms.
         */
        private List overrideBlackListedSignatureSigningAlgorithms;
        /**
         * The Override white listed signature signing algorithms.
         */
        private List overrideWhiteListedSignatureSigningAlgorithms;
        /**
         * The Override signature canonicalization algorithm.
         */
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
