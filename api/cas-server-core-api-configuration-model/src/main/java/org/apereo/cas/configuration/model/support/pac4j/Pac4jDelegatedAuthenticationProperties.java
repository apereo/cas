package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.model.support.pac4j.cas.Pac4jCasClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.oauth.Pac4jOAuth20ClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.oidc.Pac4jOidcClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.saml.Pac4jSamlClientProperties;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPDiscoveryProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RestEndpointProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link Pac4jDelegatedAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class Pac4jDelegatedAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = 4388567744591488495L;

    /**
     * When constructing the final user profile from
     * the delegated provider, determines if the provider id
     * should be combined with the principal id.
     */
    private boolean typedIdUsed;

    /**
     * The attribute to use as the principal identifier built during and upon a successful authentication attempt.
     */
    private String principalAttributeId;

    /**
     * Whether initialization of delegated identity providers should be done
     * eagerly typically during startup.
     */
    private boolean lazyInit = true;

    /**
     * Indicates whether profiles and other session data,
     * collected as part of pac4j flows and requests
     * that are kept by the container session, should be replicated
     * across the cluster using CAS and its own ticket registry.
     * Without this option, profile data and other related
     * pieces of information should be manually replicated
     * via means and libraries outside of CAS.
     */
    private boolean replicateSessions = true;

    /**
     * Handle provisioning ops when establishing profiles
     * from external identity providers.
     */
    @NestedConfigurationProperty
    private Pac4jDelegatedAuthenticationProvisioningProperties provisioning = new Pac4jDelegatedAuthenticationProvisioningProperties();

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
    private List<Pac4jSamlClientProperties> saml = new ArrayList<>(0);

    /**
     * Settings that deal with having OpenID Connect Providers as an external delegated-to authentication provider.
     */
    private List<Pac4jOidcClientProperties> oidc = new ArrayList<>(0);

    /**
     * Settings that deal with having OAuth2-capable providers as an external delegated-to authentication provider.
     */
    private List<Pac4jOAuth20ClientProperties> oauth2 = new ArrayList<>(0);

    /**
     * Settings that deal with having CAS Servers as an external delegated-to authentication provider.
     */
    private List<Pac4jCasClientProperties> cas = new ArrayList<>(0);

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
     * Settings that deal with having HiOrg-Server as an external delegated-to
     * authentication provider.
     */
    private HiOrgServer hiOrgServer = new HiOrgServer();

    /**
     * The name of the authentication handler in CAS used for delegation.
     */
    private String name;

    /**
     * Order of the authentication handler in the chain.
     */
    private Integer order;

    /**
     * Settings related to handling saml2 discovery of IdPs.
     */
    @NestedConfigurationProperty
    private SamlIdPDiscoveryProperties samlDiscovery = new SamlIdPDiscoveryProperties();

    /**
     * Settings that allow CAS to fetch and build clients
     * over a REST endpoint rather than built-in properties.
     */
    private Rest rest = new Rest();

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class LinkedIn extends Pac4jIdentifiableClientProperties {

        private static final long serialVersionUID = 4633395854143281872L;

        /**
         * The requested scope.
         */
        private String scope;

        public LinkedIn() {
            setClientName("LinkedIn");
        }
    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Facebook extends Pac4jIdentifiableClientProperties {

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
    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Bitbucket extends Pac4jIdentifiableClientProperties {

        private static final long serialVersionUID = -6189494666598669078L;

        public Bitbucket() {
            setClientName("Bitbucket");
        }
    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Wordpress extends Pac4jIdentifiableClientProperties {

        private static final long serialVersionUID = 4636855941699435914L;

        public Wordpress() {
            setClientName("Wordpress");
        }
    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Paypal extends Pac4jIdentifiableClientProperties {

        private static final long serialVersionUID = -5663033494303169583L;

        public Paypal() {
            setClientName("Paypal");
        }
    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Twitter extends Pac4jIdentifiableClientProperties {

        private static final long serialVersionUID = 6906343970517008092L;

        /**
         * Set to true to request the user's email address from the Twitter API.
         * For this to have an effect it must first be enabled in the Twitter developer console.
         */
        private boolean includeEmail;

        public Twitter() {
            setClientName("Twitter");
        }
    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Github extends Pac4jIdentifiableClientProperties {

        private static final long serialVersionUID = 9217581995885784515L;

        /**
         * The requested scope from the provider.
         */
        private String scope;

        public Github() {
            setClientName("Github");
        }

    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Yahoo extends Pac4jIdentifiableClientProperties {

        private static final long serialVersionUID = 8011580257047982361L;

        public Yahoo() {
            setClientName("Yahoo");
        }
    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Foursquare extends Pac4jIdentifiableClientProperties {

        private static final long serialVersionUID = -1784820695301605307L;

        public Foursquare() {
            setClientName("Foursquare");
        }
    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Dropbox extends Pac4jIdentifiableClientProperties {

        private static final long serialVersionUID = -1508055128010569953L;

        public Dropbox() {
            setClientName("Dropbox");
        }
    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class HiOrgServer extends Pac4jIdentifiableClientProperties {
        private static final long serialVersionUID = -1898237349924741147L;

        /**
         * The requested scope.
         */
        private String scope;

        public HiOrgServer() {
            setClientName("HiOrg-Server");
        }
    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Orcid extends Pac4jIdentifiableClientProperties {

        private static final long serialVersionUID = 1337923364401817796L;

        public Orcid() {
            setClientName("ORCID");
        }
    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class WindowsLive extends Pac4jIdentifiableClientProperties {

        private static final long serialVersionUID = -1816309711278174847L;

        public WindowsLive() {
            setClientName("Windows Live");
        }
    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow")
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Google extends Pac4jIdentifiableClientProperties {

        private static final long serialVersionUID = -3023053058552426312L;

        /**
         * The requested scope from the provider.
         */
        private String scope;

        public Google() {
            setClientName("Google");
        }
    }

    @RequiresModule(name = "cas-server-support-pac4j-webflow", automated = true)
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Rest extends RestEndpointProperties {
        private static final long serialVersionUID = 3659099897056632608L;
    }
}
