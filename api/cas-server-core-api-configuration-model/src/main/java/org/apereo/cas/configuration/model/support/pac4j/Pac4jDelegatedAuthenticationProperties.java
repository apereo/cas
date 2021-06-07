package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.model.support.pac4j.cas.Pac4jCasClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.oauth.Pac4jOAuth20ClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.oidc.Pac4jOidcClientProperties;
import org.apereo.cas.configuration.model.support.pac4j.saml.Pac4jSamlClientProperties;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPDiscoveryProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
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
@JsonFilter("Pac4jDelegatedAuthenticationProperties")
public class Pac4jDelegatedAuthenticationProperties implements Serializable {

    private static final long serialVersionUID = 4388567744591488495L;

    /**
     * Pac4j core authentication engine settings.
     */
    @NestedConfigurationProperty
    private Pac4jDelegatedAuthenticationCoreProperties core = new Pac4jDelegatedAuthenticationCoreProperties();

    /**
     * Handle provisioning ops when establishing profiles
     * from external identity providers.
     */
    @NestedConfigurationProperty
    private Pac4jDelegatedAuthenticationProvisioningProperties provisioning = new Pac4jDelegatedAuthenticationProvisioningProperties();

    /**
     * Settings that deal with having Facebook as an external delegated-to authentication provider.
     */
    @NestedConfigurationProperty
    private Pac4jDelegatedAuthenticationFacebookProperties facebook = new Pac4jDelegatedAuthenticationFacebookProperties();

    /**
     * Settings that deal with having Twitter as an external delegated-to authentication provider.
     */
    @NestedConfigurationProperty
    private Pac4jDelegatedAuthenticationTwitterProperties twitter = new Pac4jDelegatedAuthenticationTwitterProperties();

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
    @NestedConfigurationProperty
    private Pac4jDelegatedAuthenticationLinkedInProperties linkedIn = new Pac4jDelegatedAuthenticationLinkedInProperties();

    /**
     * Settings that deal with having Dropbox as an external delegated-to authentication provider.
     */
    @NestedConfigurationProperty
    private Pac4jDelegatedAuthenticationDropboxProperties dropbox = new Pac4jDelegatedAuthenticationDropboxProperties();

    /**
     * Settings that deal with having Github as an external delegated-to authentication provider.
     */
    @NestedConfigurationProperty
    private Pac4jDelegatedAuthenticationGitHubProperties github = new Pac4jDelegatedAuthenticationGitHubProperties();

    /**
     * Settings that deal with having Google as an external delegated-to authentication provider.
     */
    @NestedConfigurationProperty
    private Pac4jDelegatedAuthenticationGoogleProperties google = new Pac4jDelegatedAuthenticationGoogleProperties();

    /**
     * Settings that deal with having Yahoo as an external delegated-to authentication provider.
     */
    @NestedConfigurationProperty
    private Pac4jDelegatedAuthenticationYahooProperties yahoo = new Pac4jDelegatedAuthenticationYahooProperties();

    /**
     * Settings that deal with having FourSquare as an external delegated-to authentication provider.
     */
    @NestedConfigurationProperty
    private Pac4jDelegatedAuthenticationFoursquareProperties foursquare = new Pac4jDelegatedAuthenticationFoursquareProperties();

    /**
     * Settings that deal with having WindowsLive as an external delegated-to authentication provider.
     */
    @NestedConfigurationProperty
    private Pac4jDelegatedAuthenticationWindowsLiveProperties windowsLive = new Pac4jDelegatedAuthenticationWindowsLiveProperties();

    /**
     * Settings that deal with having Paypal as an external delegated-to authentication provider.
     */
    @NestedConfigurationProperty
    private Pac4jDelegatedAuthenticationPayPalProperties paypal = new Pac4jDelegatedAuthenticationPayPalProperties();

    /**
     * Settings that deal with having WordPress as an external delegated-to authentication provider.
     */
    @NestedConfigurationProperty
    private Pac4jDelegatedAuthenticationWordpressProperties wordpress = new Pac4jDelegatedAuthenticationWordpressProperties();

    /**
     * Settings that deal with having BitBucket as an external delegated-to authentication provider.
     */
    @NestedConfigurationProperty
    private Pac4jDelegatedAuthenticationBitBucketProperties bitbucket = new Pac4jDelegatedAuthenticationBitBucketProperties();

    /**
     * Settings that deal with having HiOrg-Server as an external delegated-to
     * authentication provider.
     */
    @NestedConfigurationProperty
    private Pac4jDelegatedAuthenticationHiOrgServerProperties hiOrgServer = new Pac4jDelegatedAuthenticationHiOrgServerProperties();

    /**
     * Settings related to handling saml2 discovery of IdPs.
     */
    @NestedConfigurationProperty
    private SamlIdPDiscoveryProperties samlDiscovery = new SamlIdPDiscoveryProperties();

    /**
     * Settings that allow CAS to fetch and build clients
     * over a REST endpoint rather than built-in properties.
     */
    @NestedConfigurationProperty
    private Pac4jDelegatedAuthenticationRestfulProperties rest = new Pac4jDelegatedAuthenticationRestfulProperties();

    /**
     * Cookie settings to be used with delegated authentication
     * to store user preferences.
     */
    @NestedConfigurationProperty
    private Pac4jDelegatedAuthenticationCookieProperties cookie = new Pac4jDelegatedAuthenticationCookieProperties();
}
