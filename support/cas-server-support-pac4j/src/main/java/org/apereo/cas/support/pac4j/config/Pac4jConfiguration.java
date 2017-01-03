package org.apereo.cas.support.pac4j.config;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.DefaultPrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.pac4j.authentication.ClientAuthenticationMetaDataPopulator;
import org.apereo.cas.support.pac4j.authentication.handler.support.ClientAuthenticationHandler;
import org.apereo.cas.support.pac4j.web.flow.DelegatedClientAuthenticationAction;
import org.apereo.cas.support.pac4j.web.flow.SAML2ClientLogoutAction;
import org.apereo.cas.util.http.HttpClient;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.pac4j.config.client.PropertiesConfigFactory;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link Pac4jConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("pac4jConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class Pac4jConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;
    
    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;
    
    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map<AuthenticationHandler, PrincipalResolver> authenticationHandlersResolvers;

    @Autowired
    @Qualifier("authenticationMetadataPopulators")
    private List<AuthenticationMetaDataPopulator> authenticationMetadataPopulators;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Bean
    public PrincipalFactory clientPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    public AuthenticationMetaDataPopulator clientAuthenticationMetaDataPopulator() {
        return new ClientAuthenticationMetaDataPopulator();
    }

    @Bean
    public Action saml2ClientLogoutAction() {
        return new SAML2ClientLogoutAction(builtClients(), httpClient);
    }
    
    @RefreshScope
    @Bean
    public AuthenticationHandler clientAuthenticationHandler() {
        final ClientAuthenticationHandler h = new ClientAuthenticationHandler();
        h.setClients(builtClients());
        h.setPrincipalFactory(clientPrincipalFactory());
        h.setServicesManager(servicesManager);
        h.setTypedIdUsed(casProperties.getAuthn().getPac4j().isTypedIdUsed());
        h.setName(casProperties.getAuthn().getPac4j().getName());
        return h;
    }

    @RefreshScope
    @Bean
    public Action clientAction() {
        return new DelegatedClientAuthenticationAction(builtClients(), 
                authenticationSystemSupport, 
                centralAuthenticationService, 
                casProperties.getTheme().getParamName(), 
                casProperties.getLocale().getParamName(), 
                casProperties.getAuthn().getPac4j().isAutoRedirect());
    }

    private void configureGithubClient(final Map<String, String> properties) {
        final Pac4jProperties.Github github = casProperties.getAuthn().getPac4j().getGithub();
        properties.put(PropertiesConfigFactory.GITHUB_ID, github.getId());
        properties.put(PropertiesConfigFactory.GITHUB_SECRET, github.getSecret());
    }

    private void configureDropboxClient(final Map<String, String> properties) {
        final Pac4jProperties.Dropbox db = casProperties.getAuthn().getPac4j().getDropbox();
        properties.put(PropertiesConfigFactory.DROPBOX_ID, db.getId());
        properties.put(PropertiesConfigFactory.DROPBOX_SECRET, db.getSecret());
    }

    private void configureWindowsLiveClient(final Map<String, String> properties) {
        final Pac4jProperties.WindowsLive live = casProperties.getAuthn().getPac4j().getWindowsLive();
        properties.put(PropertiesConfigFactory.WINDOWSLIVE_ID, live.getId());
        properties.put(PropertiesConfigFactory.WINDOWSLIVE_SECRET, live.getSecret());
    }

    private void configureYahooClient(final Map<String, String> properties) {
        final Pac4jProperties.Yahoo yahoo = casProperties.getAuthn().getPac4j().getYahoo();
        properties.put(PropertiesConfigFactory.YAHOO_ID, yahoo.getId());
        properties.put(PropertiesConfigFactory.YAHOO_SECRET, yahoo.getSecret());
    }

    private void configureFoursquareClient(final Map<String, String> properties) {
        final Pac4jProperties.Foursquare foursquare = casProperties.getAuthn().getPac4j().getFoursquare();
        properties.put(PropertiesConfigFactory.FOURSQUARE_ID, foursquare.getId());
        properties.put(PropertiesConfigFactory.FOURSQUARE_SECRET, foursquare.getSecret());
    }

    private void configureGoogleClient(final Map<String, String> properties) {
        final Pac4jProperties.Google google = casProperties.getAuthn().getPac4j().getGoogle();
        properties.put(PropertiesConfigFactory.GOOGLE_ID, google.getId());
        properties.put(PropertiesConfigFactory.GOOGLE_SECRET, google.getSecret());
        properties.put(PropertiesConfigFactory.GOOGLE_SCOPE, google.getScope());
    }

    private void configureFacebookClient(final Map<String, String> properties) {
        final Pac4jProperties.Facebook fb = casProperties.getAuthn().getPac4j().getFacebook();
        properties.put(PropertiesConfigFactory.FACEBOOK_ID, fb.getId());
        properties.put(PropertiesConfigFactory.FACEBOOK_SECRET, fb.getSecret());
        properties.put(PropertiesConfigFactory.FACEBOOK_SCOPE, fb.getScope());
        properties.put(PropertiesConfigFactory.FACEBOOK_FIELDS, fb.getFields());
    }

    private void configureLinkedInClient(final Map<String, String> properties) {
        final Pac4jProperties.LinkedIn fb = casProperties.getAuthn().getPac4j().getLinkedIn();
        properties.put(PropertiesConfigFactory.LINKEDIN_ID, fb.getId());
        properties.put(PropertiesConfigFactory.LINKEDIN_SECRET, fb.getSecret());
        properties.put(PropertiesConfigFactory.LINKEDIN_SCOPE, fb.getScope());
        properties.put(PropertiesConfigFactory.LINKEDIN_FIELDS, fb.getFields());
    }

    private void configureTwitterClient(final Map<String, String> properties) {
        final Pac4jProperties.Twitter twitter = casProperties.getAuthn().getPac4j().getTwitter();
        properties.put(PropertiesConfigFactory.TWITTER_ID, twitter.getId());
        properties.put(PropertiesConfigFactory.TWITTER_SECRET, twitter.getSecret());
    }

    private void configureCasClient(final Map<String, String> properties) {
        final Pac4jProperties.Cas cas = casProperties.getAuthn().getPac4j().getCas();
        properties.put(PropertiesConfigFactory.CAS_LOGIN_URL, cas.getLoginUrl());
        properties.put(PropertiesConfigFactory.CAS_PROTOCOL, cas.getProtocol());
    }

    private void configureSamlClient(final Map<String, String> properties) {
        final Pac4jProperties.Saml saml = casProperties.getAuthn().getPac4j().getSaml();

        properties.put(PropertiesConfigFactory.SAML_IDENTITY_PROVIDER_METADATA_PATH, saml.getIdentityProviderMetadataPath());
        properties.put(PropertiesConfigFactory.SAML_KEYSTORE_PASSWORD, saml.getKeystorePassword());
        properties.put(PropertiesConfigFactory.SAML_KEYSTORE_PATH, saml.getKeystorePath());
        properties.put(PropertiesConfigFactory.SAML_MAXIMUM_AUTHENTICATION_LIFETIME, saml.getMaximumAuthenticationLifetime());
        properties.put(PropertiesConfigFactory.SAML_PRIVATE_KEY_PASSWORD, saml.getPrivateKeyPassword());
        properties.put(PropertiesConfigFactory.SAML_SERVICE_PROVIDER_ENTITY_ID, saml.getServiceProviderEntityId());
        properties.put(PropertiesConfigFactory.SAML_SERVICE_PROVIDER_METADATA_PATH, saml.getServiceProviderMetadataPath());
        properties.put(PropertiesConfigFactory.SAML_DESTINATION_BINDING_TYPE, SAMLConstants.SAML2_REDIRECT_BINDING_URI);
    }

    private void configureOidcClient(final Map<String, String> properties) {
        final Pac4jProperties.Oidc oidc = casProperties.getAuthn().getPac4j().getOidc();

        properties.put(PropertiesConfigFactory.OIDC_CUSTOM_PARAM_KEY1, oidc.getCustomParamKey1());
        properties.put(PropertiesConfigFactory.OIDC_CUSTOM_PARAM_KEY2, oidc.getCustomParamKey2());
        properties.put(PropertiesConfigFactory.OIDC_CUSTOM_PARAM_VALUE1, oidc.getCustomParamValue1());
        properties.put(PropertiesConfigFactory.OIDC_CUSTOM_PARAM_VALUE2, oidc.getCustomParamValue2());
        properties.put(PropertiesConfigFactory.OIDC_DISCOVERY_URI, oidc.getDiscoveryUri());
        properties.put(PropertiesConfigFactory.OIDC_ID, oidc.getId());
        properties.put(PropertiesConfigFactory.OIDC_MAX_CLOCK_SKEW, oidc.getMaxClockSkew());
        properties.put(PropertiesConfigFactory.OIDC_PREFERRED_JWS_ALGORITHM, oidc.getPreferredJwsAlgorithm());
        properties.put(PropertiesConfigFactory.OIDC_SECRET, oidc.getSecret());
        properties.put(PropertiesConfigFactory.OIDC_USE_NONCE, oidc.getUseNonce());
        properties.put(PropertiesConfigFactory.OIDC_SCOPE, oidc.getScope());
    }

    /**
     * Returning the built clients.
     *
     * @return the built clients.
     */
    @RefreshScope
    @Bean
    public Clients builtClients() {
        final List<Client> allClients = new ArrayList<>();

        // turn the properties file into a map of properties
        final Map<String, String> properties = new HashMap<>();

        configureCasClient(properties);
        configureFacebookClient(properties);
        configureOidcClient(properties);
        configureSamlClient(properties);
        configureTwitterClient(properties);
        configureDropboxClient(properties);
        configureFoursquareClient(properties);
        configureGithubClient(properties);
        configureGoogleClient(properties);
        configureWindowsLiveClient(properties);
        configureYahooClient(properties);
        configureLinkedInClient(properties);

        // add the new clients found via properties first
        final ConfigFactory configFactory = new PropertiesConfigFactory(properties);
        final Config propertiesConfig = configFactory.build();
        allClients.addAll(propertiesConfig.getClients().getClients());
        
        if (allClients.isEmpty()) {
            throw new IllegalArgumentException("At least one client must be defined");
        }
        return new Clients(casProperties.getServer().getLoginUrl(), allClients);
    }


    @PostConstruct
    protected void initializeRootApplicationContext() {
        authenticationHandlersResolvers.put(clientAuthenticationHandler(), this.personDirectoryPrincipalResolver);
        authenticationMetadataPopulators.add(0, clientAuthenticationMetaDataPopulator());
    }
}
