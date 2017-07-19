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

    /**
     * When constructing the final user profile from
     * the delegated provider, determines if the provider id
     * should be combined with the principal id.
     */
    private boolean typedIdUsed;
    /**
     * Whether CAS should auto-redirect to the provider.
     */
    private boolean autoRedirect;

    /**
     * Settings that deal with having Facebook as an external delegated-to authentication provider.
     */
    private Facebook facebook = new Facebook();
    /**
     * Settings that deal with having Twitter as an external delegated-to authentication provider.
     */
    private Twitter twitter = new Twitter();
    /**
     * Settings that deal with having SAML2 IdPs as an external delegated-to authentication provider.
     */
    private List<Saml> saml = new ArrayList<>();
    /**
     * Settings that deal with having OpenID Connect Providers as an external delegated-to authentication provider.
     */
    private List<Oidc> oidc = new ArrayList<>();
    /**
     * Settings that deal with having OAuth2-capable providers as an external delegated-to authentication provider.
     */
    private List<OAuth20> oauth2 = new ArrayList<>();

    /**
     * Settings that deal with having CAS Servers as an external delegated-to authentication provider.
     */
    private List<Cas> cas = new ArrayList<>();

    /**
     * Settings that deal with having LinkedIn as an external delegated-to authentication provider.
     */
    private LinkedIn linkedIn = new LinkedIn();
    /**
     * Settings that deal with having Dropbox as an external delegated-to authentication provider.
     */
    private Dropbox dropbox = new Dropbox();
    /**
     * Settings that deal with having Orcid as an external delegated-to authentication provider.
     */
    private Orcid orcid = new Orcid();
    /**
     * Settings that deal with having Github as an external delegated-to authentication provider.
     */
    private Github github = new Github();
    /**
     * Settings that deal with having Google as an external delegated-to authentication provider.
     */
    private Google google = new Google();
    /**
     * Settings that deal with having Yahoo as an external delegated-to authentication provider.
     */
    private Yahoo yahoo = new Yahoo();
    /**
     * Settings that deal with having FourSquare as an external delegated-to authentication provider.
     */
    private Foursquare foursquare = new Foursquare();
    /**
     * Settings that deal with having WindowsLive as an external delegated-to authentication provider.
     */
    private WindowsLive windowsLive = new WindowsLive();
    /**
     * Settings that deal with having Paypal as an external delegated-to authentication provider.
     */
    private Paypal paypal = new Paypal();
    /**
     * Settings that deal with having WordPress as an external delegated-to authentication provider.
     */
    private Wordpress wordpress = new Wordpress();
    /**
     * Settings that deal with having BitBucket as an external delegated-to authentication provider.
     */
    private Bitbucket bitbucket = new Bitbucket();

    /**
     * The name of the authentication handler in CAS used for delegation.
     */
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

    public Orcid getOrcid() {
        return orcid;
    }

    public void setOrcid(final Orcid orcid) {
        this.orcid = orcid;
    }

    public static class LinkedIn extends Pac4jGenericClientProperties {

        /**
         * The requested scope.
         */
        private String scope;
        /**
         * Custom fields to include in the request.
         */
        private String fields;

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

    public static class Facebook extends Pac4jGenericClientProperties {
        /**
         * The requested scope.
         */
        private String scope;
        /**
         * Custom fields to include in the request.
         */
        private String fields;


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

    public static class Bitbucket extends Pac4jGenericClientProperties {
    }

    public static class Wordpress extends Pac4jGenericClientProperties {
    }

    public static class Paypal extends Pac4jGenericClientProperties {

    }

    public static class OAuth20 extends Pac4jGenericClientProperties {
        /**
         * Authorization endpoint of the provider.
         */
        private String authUrl;
        /**
         * Token endpoint of the provider.
         */
        private String tokenUrl;
        /**
         * Profile endpoint of the provider.
         */
        private String profileUrl;
        /**
         * Profile path portion of the profile endpoint of the provider.
         */
        private String profilePath;
        /**
         * Http method to use when asking for profile.
         */
        private String profileVerb = "POST";

        /**
         * Profile attributes to request and collect in form of key-value pairs.
         */
        private Map<String, String> profileAttrs;

        /**
         * Custsom parameters in form of key-value pairs sent along in authZ requests, etc.
         */
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

    }

    public static class Twitter extends Pac4jGenericClientProperties {
    }

    public static class Saml {
        /**
         * The password to use when generating the SP/CAS keystore.
         */
        private String keystorePassword;
        /**
         * The password to use when generating the private key for the SP/CAS keystore.
         */
        private String privateKeyPassword;
        /**
         * Location of the keystore to use and generate the SP/CAS keystore.
         */
        private String keystorePath;
        /**
         * The metadata location of the identity provider that is to handle authentications.
         */
        private String identityProviderMetadataPath;
        /**
         * Once you have an authenticated session on the identity provider, usually it won't prompt you again to enter your
         * credentials and it will automatically generate a new assertion for you. By default, the SAML client
         * will accept assertions based on a previous authentication for one hour.
         * You can adjust this behavior by modifying this setting. The unit of time here is seconds.
         */
        private int maximumAuthenticationLifetime = 600;

        /**
         * The entity id of the SP/CAS that is used in the SP metadata generation process.
         */
        private String serviceProviderEntityId;
        /**
         * Location of the SP metadata to use and generate.
         */
        private String serviceProviderMetadataPath;

        /**
         * Name of the SAML client mostly for UI purposes and uniqueness.
         */
        private String clientName;

        /**
         * Whether authentication requests should be tagged as forced auth.
         */
        private boolean forceAuth;
        /**
         * Whether authentication requests should be tagged as passive.
         */
        private boolean passive;

        /**
         * Requested authentication context class in authn requests.
         */
        private String authnContextClassRef;
        /**
         * Specifies the comparison rule that should be used to evaluate the specified authentication methods.
         * For example, if “exact” is specified, the authentication method used must match one of the authentication
         * methods specified by the AuthnContextClassRef elements.
         * AuthContextClassRef element require comparison rule to be used to evaluate the specified
         * authentication methods. If not explicitly specified "exact" rule will be used by default.
         * Other acceptable values are minimum, maximum, better.
         */
        private String authnContextComparisonType = "exact";

        /**
         * The key alias used in the keystore.
         */
        private String keystoreAlias;
        /**
         * NameID policy to request in the authentication requests.
         */
        private String nameIdPolicyFormat;
        /**
         * Whether metadata should be marked to request sign assertions.
         */
        private boolean wantsAssertionsSigned;

        public boolean isPassive() {
            return passive;
        }

        public void setPassive(final boolean passive) {
            this.passive = passive;
        }

        public boolean isForceAuth() {
            return forceAuth;
        }

        public void setForceAuth(final boolean forceAuth) {
            this.forceAuth = forceAuth;
        }

        public String getAuthnContextClassRef() {
            return authnContextClassRef;
        }

        public void setAuthnContextClassRef(final String authnContextClassRef) {
            this.authnContextClassRef = authnContextClassRef;
        }

        public String getAuthnContextComparisonType() {
            return authnContextComparisonType;
        }

        public void setAuthnContextComparisonType(final String authnContextComparisonType) {
            this.authnContextComparisonType = authnContextComparisonType;
        }

        public String getKeystoreAlias() {
            return keystoreAlias;
        }

        public void setKeystoreAlias(final String keystoreAlias) {
            this.keystoreAlias = keystoreAlias;
        }

        public String getNameIdPolicyFormat() {
            return nameIdPolicyFormat;
        }

        public void setNameIdPolicyFormat(final String nameIdPolicyFormat) {
            this.nameIdPolicyFormat = nameIdPolicyFormat;
        }

        public boolean isWantsAssertionsSigned() {
            return wantsAssertionsSigned;
        }

        public void setWantsAssertionsSigned(final boolean wantsAssertionsSigned) {
            this.wantsAssertionsSigned = wantsAssertionsSigned;
        }

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

        public String getClientName() {
            return clientName;
        }

        public void setClientName(final String clientName) {
            this.clientName = clientName;
        }
    }

    public static class Cas {
        /**
         * The CAS server login url.
         */
        private String loginUrl;
        /**
         * CAS protocol to use.
         * Acceptable values are <code>CAS10, CAS20, CAS20_PROXY, CAS30, CAS30_PROXY, SAML</code>.
         */
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

    public static class Oidc extends Pac4jGenericClientProperties {
        /**
         * The type of the provider. "google" and "azure" are also acceptable values.
         */
        private String type = "generic";
        /**
         * The discovery endpoint to locate the provide metadata.
         */
        private String discoveryUri;

        /**
         * Whether an initial nonce should be to used
         * initially for replay attack mitigation.
         */
        private boolean useNonce;

        /**
         * Requested scope(s).
         */
        private String scope;
        /**
         * The JWS algorithm to use forcefully when validating ID tokens.
         * If none is defined, the first algorithm from metadata will be used.
         */
        private String preferredJwsAlgorithm;

        /**
         * Clock skew in order to account for drift, when validating id tokens.
         */
        private int maxClockSkew;

        /**
         * Custom parameters to send along in authZ requests, etc.
         */
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

    public static class Github extends Pac4jGenericClientProperties {
    }

    public static class Yahoo extends Pac4jGenericClientProperties {
    }

    public static class Foursquare extends Pac4jGenericClientProperties {
    }

    public static class Dropbox extends Pac4jGenericClientProperties {
    }

    public static class Orcid extends Pac4jGenericClientProperties {
    }

    public static class WindowsLive extends Pac4jGenericClientProperties {
    }

    public static class Google extends Pac4jGenericClientProperties {
        /**
         * The requested scope from the provider.
         */
        private String scope;

        public String getScope() {
            return scope;
        }

        public void setScope(final String scope) {
            this.scope = scope;
        }
    }
}
