package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link Pac4jProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
public class Pac4jProperties implements Serializable {

    private static final long serialVersionUID = 4388567744591488495L;
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
    private List<Pac4jSamlProperties> saml = new ArrayList<>();
    /**
     * Settings that deal with having OpenID Connect Providers as an external delegated-to authentication provider.
     */
    private List<Pac4jOidcProperties> oidc = new ArrayList<>();
    /**
     * Settings that deal with having OAuth2-capable providers as an external delegated-to authentication provider.
     */
    private List<Pac4jOAuth20Properties> oauth2 = new ArrayList<>();

    /**
     * Settings that deal with having CAS Servers as an external delegated-to authentication provider.
     */
    private List<Pac4jCasProperties> cas = new ArrayList<>();

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

    public List<Pac4jOAuth20Properties> getOauth2() {
        return oauth2;
    }

    public void setOauth2(final List<Pac4jOAuth20Properties> oauth2) {
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

    public void setSaml(final List<Pac4jSamlProperties> saml) {
        this.saml = saml;
    }

    public void setOidc(final List<Pac4jOidcProperties> oidc) {
        this.oidc = oidc;
    }

    public void setCas(final List<Pac4jCasProperties> cas) {
        this.cas = cas;
    }

    public List<Pac4jCasProperties> getCas() {
        return this.cas;
    }

    public List<Pac4jSamlProperties> getSaml() {
        return this.saml;
    }

    public List<Pac4jOidcProperties> getOidc() {
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

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    public static class LinkedIn extends Pac4jGenericClientProperties {

        private static final long serialVersionUID = 4633395854143281872L;
        /**
         * The requested scope.
         */
        private String scope;
        /**
         * Custom fields to include in the request.
         */
        private String fields;

        public LinkedIn() {
            setClientName("LinkedIn");
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

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    public static class Facebook extends Pac4jGenericClientProperties {
        private static final long serialVersionUID = -2737594266552466076L;
        /**
         * The requested scope.
         */
        private String scope;
        /**
         * Custom fields to include in the request.
         */
        private String fields;

        public Facebook() {
            setClientName("Facebook");
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

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    public static class Bitbucket extends Pac4jGenericClientProperties {
        private static final long serialVersionUID = -6189494666598669078L;

        public Bitbucket() { 
            setClientName("Bitbucket");
        }
    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    public static class Wordpress extends Pac4jGenericClientProperties {
        private static final long serialVersionUID = 4636855941699435914L;
        public Wordpress() {
            setClientName("Wordpress");
        }
    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    public static class Paypal extends Pac4jGenericClientProperties {
        private static final long serialVersionUID = -5663033494303169583L;
        public Paypal() {
            setClientName("Paypal");
        }
    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    public static class Twitter extends Pac4jGenericClientProperties {
        private static final long serialVersionUID = 6906343970517008092L;
        public Twitter() {
            setClientName("Twitter");
        }
    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    public static class Github extends Pac4jGenericClientProperties {
        private static final long serialVersionUID = 9217581995885784515L;
        public Github() {
            setClientName("Github");
        }
    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    public static class Yahoo extends Pac4jGenericClientProperties {
        private static final long serialVersionUID = 8011580257047982361L;
        public Yahoo() {
            setClientName("Yahoo");
        }
    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    public static class Foursquare extends Pac4jGenericClientProperties {
        private static final long serialVersionUID = -1784820695301605307L;
        public Foursquare() {
            setClientName("Foursquare");
        }
    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    public static class Dropbox extends Pac4jGenericClientProperties {
        private static final long serialVersionUID = -1508055128010569953L;
        public Dropbox() {
            setClientName("Dropbox");
        }
    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    public static class Orcid extends Pac4jGenericClientProperties {
        private static final long serialVersionUID = 1337923364401817796L;
        public Orcid() {
            setClientName("ORCID");
        }
    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    public static class WindowsLive extends Pac4jGenericClientProperties {
        private static final long serialVersionUID = -1816309711278174847L;
        public WindowsLive() {
            setClientName("Windows Live");
        }
    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    public static class Google extends Pac4jGenericClientProperties {
        private static final long serialVersionUID = -3023053058552426312L;
        /**
         * The requested scope from the provider.
         */
        private String scope;

        public Google() {
            setClientName("Google");
        }
        
        public String getScope() {
            return scope;
        }

        public void setScope(final String scope) {
            this.scope = scope;
        }
    }
}
