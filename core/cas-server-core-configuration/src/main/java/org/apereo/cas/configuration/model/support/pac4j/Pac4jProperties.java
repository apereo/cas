package org.apereo.cas.configuration.model.support.pac4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link Pac4jProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class Pac4jProperties {

    private boolean typedIdUsed;
    private boolean autoRedirect;

    private Facebook facebook = new Facebook();
    private Twitter twitter = new Twitter();
    private List<Saml> saml = new ArrayList<>();
    private List<Oidc> oidc = new ArrayList<>();
    private List<OAuth20> oauth2 = new ArrayList<>();
    private List<Cas> cas = new ArrayList<>();

    private LinkedIn linkedIn = new LinkedIn();
    private Dropbox dropbox = new Dropbox();
    private Github github = new Github();
    private Google google = new Google();
    private Yahoo yahoo = new Yahoo();
    private Foursquare foursquare = new Foursquare();
    private WindowsLive windowsLive = new WindowsLive();
    private Paypal paypal = new Paypal();
    private Wordpress wordpress = new Wordpress();
    private Bitbucket bitbucket = new Bitbucket();

    private String name;

    public List<OAuth20> getOauth2() {
        return oauth2;
    }

    public void setOauth2(final List<OAuth20> oauth2) {
        this.oauth2 = oauth2;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isAutoRedirect() {
        return autoRedirect;
    }

    public void setAutoRedirect(final boolean autoRedirect) {
        this.autoRedirect = autoRedirect;
    }

    public Bitbucket getBitbucket() {
        return bitbucket;
    }

    public void setBitbucket(final Bitbucket bitbucket) {
        this.bitbucket = bitbucket;
    }

    public Wordpress getWordpress() {
        return wordpress;
    }

    public void setWordpress(final Wordpress wordpress) {
        this.wordpress = wordpress;
    }

    public Paypal getPaypal() {
        return paypal;
    }

    public void setPaypal(final Paypal paypal) {
        this.paypal = paypal;
    }

    public LinkedIn getLinkedIn() {
        return linkedIn;
    }

    public void setLinkedIn(final LinkedIn linkedIn) {
        this.linkedIn = linkedIn;
    }

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

    public void setSaml(final List<Saml> saml) {
        this.saml = saml;
    }

    public void setOidc(final List<Oidc> oidc) {
        this.oidc = oidc;
    }

    public void setCas(final List<Cas> cas) {
        this.cas = cas;
    }

    public List<Cas> getCas() {
        return this.cas;
    }

    public List<Saml> getSaml() {
        return this.saml;
    }

    public List<Oidc> getOidc() {
        return this.oidc;
    }

    public Facebook getFacebook() {
        return this.facebook;
    }

    public Twitter getTwitter() {
        return this.twitter;
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

    public static class Bitbucket {
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

    public static class Wordpress {
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

    public static class Paypal {
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

    public static class OAuth20 {
        private String id;
        private String secret;

        private String authUrl;
        private String tokenUrl;
        private String profileUrl;
        private String profilePath;
        private String profileVerb = "POST";
        private Map<String, String> profileAttrs;
        private Map<String, String> customParams;

        public String getAuthUrl() {
            return authUrl;
        }

        public void setAuthUrl(final String authUrl) {
            this.authUrl = authUrl;
        }

        public String getTokenUrl() {
            return tokenUrl;
        }

        public void setTokenUrl(final String tokenUrl) {
            this.tokenUrl = tokenUrl;
        }

        public String getProfileUrl() {
            return profileUrl;
        }

        public void setProfileUrl(final String profileUrl) {
            this.profileUrl = profileUrl;
        }

        public String getProfilePath() {
            return profilePath;
        }

        public void setProfilePath(final String profilePath) {
            this.profilePath = profilePath;
        }

        public String getProfileVerb() {
            return profileVerb;
        }

        public void setProfileVerb(final String profileVerb) {
            this.profileVerb = profileVerb;
        }

        public Map<String, String> getProfileAttrs() {
            return profileAttrs;
        }

        public void setProfileAttrs(final Map<String, String> profileAttrs) {
            this.profileAttrs = profileAttrs;
        }

        public Map<String, String> getCustomParams() {
            return customParams;
        }

        public void setCustomParams(final Map<String, String> customParams) {
            this.customParams = customParams;
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
        private int maximumAuthenticationLifetime = 600;
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

        public int getMaximumAuthenticationLifetime() {
            return this.maximumAuthenticationLifetime;
        }

        public void setMaximumAuthenticationLifetime(final int maximumAuthenticationLifetime) {
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
        private String type = "generic";
        private String id;
        private String secret;
        private String discoveryUri;
        private boolean useNonce;
        private String scope;
        private String preferredJwsAlgorithm;
        private int maxClockSkew;
        private Map<String, String> customParams = new HashMap<>();

        public Map<String, String> getCustomParams() {
            return customParams;
        }

        public void setCustomParams(final Map<String, String> customParams) {
            this.customParams = customParams;
        }

        public String getType() {
            return type;
        }

        public void setType(final String type) {
            this.type = type;
        }

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

        public String getDiscoveryUri() {
            return this.discoveryUri;
        }

        public void setDiscoveryUri(final String discoveryUri) {
            this.discoveryUri = discoveryUri;
        }

        public boolean isUseNonce() {
            return useNonce;
        }

        public void setUseNonce(final boolean useNonce) {
            this.useNonce = useNonce;
        }

        public String getPreferredJwsAlgorithm() {
            return this.preferredJwsAlgorithm;
        }

        public void setPreferredJwsAlgorithm(final String preferredJwsAlgorithm) {
            this.preferredJwsAlgorithm = preferredJwsAlgorithm;
        }

        public int getMaxClockSkew() {
            return this.maxClockSkew;
        }

        public void setMaxClockSkew(final int maxClockSkew) {
            this.maxClockSkew = maxClockSkew;
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
}
