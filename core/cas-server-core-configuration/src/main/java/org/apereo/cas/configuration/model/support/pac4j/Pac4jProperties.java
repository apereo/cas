package org.apereo.cas.configuration.model.support.pac4j;

/**
 * This is {@link Pac4jProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class Pac4jProperties {

    private boolean typedIdUsed;

    private Facebook facebook = new Facebook();
    private Twitter twitter = new Twitter();
    private Saml saml = new Saml();
    private Oidc oidc = new Oidc();
    private Cas cas = new Cas();

    private Dropbox dropbox = new Dropbox();
    private Github github = new Github();
    private Google google = new Google();
    private Yahoo yahoo = new Yahoo();
    private Foursquare foursquare = new Foursquare();
    private WindowsLive windowsLive = new WindowsLive();
    private LinkedIn linkedIn = new LinkedIn();

    public WindowsLive getWindowsLive() {
        return windowsLive;
    }

    public void setWindowsLive(final WindowsLive windowsLive) {
        this.windowsLive = windowsLive;
    }

    public Dropbox getDropbox() {
        return dropbox;
    }

    public void setDropbox(final Dropbox dropbox) {
        this.dropbox = dropbox;
    }

    public Github getGithub() {
        return github;
    }

    public void setGithub(final Github github) {
        this.github = github;
    }

    public Google getGoogle() {
        return google;
    }

    public void setGoogle(final Google google) {
        this.google = google;
    }

    public Yahoo getYahoo() {
        return yahoo;
    }

    public void setYahoo(final Yahoo yahoo) {
        this.yahoo = yahoo;
    }

    public Foursquare getFoursquare() {
        return foursquare;
    }

    public void setFoursquare(final Foursquare foursquare) {
        this.foursquare = foursquare;
    }

    public boolean isTypedIdUsed() {
        return typedIdUsed;
    }

    public void setTypedIdUsed(final boolean typedIdUsed) {
        this.typedIdUsed = typedIdUsed;
    }

    public void setFacebook(final Facebook facebook) {
        this.facebook = facebook;
    }

    public void setTwitter(final Twitter twitter) {
        this.twitter = twitter;
    }

    public void setSaml(final Saml saml) {
        this.saml = saml;
    }

    public void setOidc(final Oidc oidc) {
        this.oidc = oidc;
    }

    public void setCas(final Cas cas) {
        this.cas = cas;
    }

    public void setLinkedIn(final LinkedIn linkedIn) {
        this.linkedIn = linkedIn;
    }

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

    public LinkedIn getLinkedIn() {
        return linkedIn;
    }

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

    public static class Saml {
        private String keystorePassword;
        private String privateKeyPassword;
        private String keystorePath;
        private String identityProviderMetadataPath;
        private String maximumAuthenticationLifetime;
        private String serviceProviderEntityId;
        private String serviceProviderMetadataPath;

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

        public String getServiceProviderMetadataPath() {
            return serviceProviderMetadataPath;
        }

        public void setServiceProviderMetadataPath(final String serviceProviderMetadataPath) {
            this.serviceProviderMetadataPath = serviceProviderMetadataPath;
        }
    }

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

    public static class Github {
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

    public static class Yahoo {
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

    public static class Foursquare {
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

    public static class Dropbox {
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

    public static class WindowsLive {
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

    public static class Google {
        private String id;
        private String secret;
        private String scope;

        public String getScope() {
            return scope;
        }

        public void setScope(final String scope) {
            this.scope = scope;
        }

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
    
    public static class LinkedIn {
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
}
