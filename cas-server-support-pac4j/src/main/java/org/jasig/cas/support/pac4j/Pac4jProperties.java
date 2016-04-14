package org.jasig.cas.support.pac4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * This is {@link Pac4jProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("pac4jProperties")
@ConfigurationProperties(
        prefix = "cas.pac4j"
)
public class Pac4jProperties {

    private Facebook facebook = new Facebook();
    private Twitter twitter = new Twitter();
    private Saml saml = new Saml();
    private Oidc oidc = new Oidc();
    private Cas cas = new Cas();

    public Cas getCas() {
        return this.cas;
    }

    public Saml getSaml() {
        return this.saml;
    }

    public Oidc getOidc() {
        return this.oidc;
    }

    public Facebook getFacebook() {
        return this.facebook;
    }

    public Twitter getTwitter() {
        return this.twitter;
    }

    /**
     * The type Facebook.
     */
    public static class Facebook {
        private String id;
        private String secret;
        private String scope;
        private String fields;

        public String getId() {
            return this.id;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public String getSecret() {
            return this.secret;
        }

        public void setSecret(final String secret) {
            this.secret = secret;
        }

        public String getScope() {
            return this.scope;
        }

        public void setScope(final String scope) {
            this.scope = scope;
        }

        public String getFields() {
            return this.fields;
        }

        public void setFields(final String fields) {
            this.fields = fields;
        }
    }

    /**
     * The type Twitter.
     */
    public static class Twitter {
        private String id;
        private String secret;

        public String getId() {
            return this.id;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public String getSecret() {
            return this.secret;
        }

        public void setSecret(final String secret) {
            this.secret = secret;
        }
    }

    /**
     * The type Saml.
     */
    public static class Saml {
        private String keystorePassword;
        private String privateKeyPassword;
        private String keystorePath;
        private String identityProviderMetadataPath;
        private String maximumAuthenticationLifetime;
        private String serviceProviderEntityId;

        public String getKeystorePassword() {
            return this.keystorePassword;
        }

        public void setKeystorePassword(final String keystorePassword) {
            this.keystorePassword = keystorePassword;
        }

        public String getPrivateKeyPassword() {
            return this.privateKeyPassword;
        }

        public void setPrivateKeyPassword(final String privateKeyPassword) {
            this.privateKeyPassword = privateKeyPassword;
        }

        public String getKeystorePath() {
            return this.keystorePath;
        }

        public void setKeystorePath(final String keystorePath) {
            this.keystorePath = keystorePath;
        }

        public String getIdentityProviderMetadataPath() {
            return this.identityProviderMetadataPath;
        }

        public void setIdentityProviderMetadataPath(final String identityProviderMetadataPath) {
            this.identityProviderMetadataPath = identityProviderMetadataPath;
        }

        public String getMaximumAuthenticationLifetime() {
            return this.maximumAuthenticationLifetime;
        }

        public void setMaximumAuthenticationLifetime(final String maximumAuthenticationLifetime) {
            this.maximumAuthenticationLifetime = maximumAuthenticationLifetime;
        }

        public String getServiceProviderEntityId() {
            return this.serviceProviderEntityId;
        }

        public void setServiceProviderEntityId(final String serviceProviderEntityId) {
            this.serviceProviderEntityId = serviceProviderEntityId;
        }
    }

    /**
     * The type Cas.
     */
    public static class Cas {
        private String loginUrl;
        private String protocol;

        public String getLoginUrl() {
            return this.loginUrl;
        }

        public void setLoginUrl(final String loginUrl) {
            this.loginUrl = loginUrl;
        }

        public String getProtocol() {
            return this.protocol;
        }

        public void setProtocol(final String protocol) {
            this.protocol = protocol;
        }
    }

    /**
     * The type Oidc.
     */
    public static class Oidc {
        private String id;
        private String secret;
        private String discoveryUri;
        private String useNonce;
        private String preferredJwsAlgorithm;
        private String maxClockSkew;
        private String customParamKey1;
        private String customParamValue1;
        private String customParamKey2;
        private String customParamValue2;

        public String getId() {
            return this.id;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public String getSecret() {
            return this.secret;
        }

        public void setSecret(final String secret) {
            this.secret = secret;
        }

        public String getDiscoveryUri() {
            return this.discoveryUri;
        }

        public void setDiscoveryUri(final String discoveryUri) {
            this.discoveryUri = discoveryUri;
        }

        public String getUseNonce() {
            return this.useNonce;
        }

        public void setUseNonce(final String useNonce) {
            this.useNonce = useNonce;
        }

        public String getPreferredJwsAlgorithm() {
            return this.preferredJwsAlgorithm;
        }

        public void setPreferredJwsAlgorithm(final String preferredJwsAlgorithm) {
            this.preferredJwsAlgorithm = preferredJwsAlgorithm;
        }

        public String getMaxClockSkew() {
            return this.maxClockSkew;
        }

        public void setMaxClockSkew(final String maxClockSkew) {
            this.maxClockSkew = maxClockSkew;
        }

        public String getCustomParamKey1() {
            return this.customParamKey1;
        }

        public void setCustomParamKey1(final String customParamKey1) {
            this.customParamKey1 = customParamKey1;
        }

        public String getCustomParamValue1() {
            return this.customParamValue1;
        }

        public void setCustomParamValue1(final String customParamValue1) {
            this.customParamValue1 = customParamValue1;
        }

        public String getCustomParamKey2() {
            return this.customParamKey2;
        }

        public void setCustomParamKey2(final String customParamKey2) {
            this.customParamKey2 = customParamKey2;
        }

        public String getCustomParamValue2() {
            return this.customParamValue2;
        }

        public void setCustomParamValue2(final String customParamValue2) {
            this.customParamValue2 = customParamValue2;
        }
    }


}
