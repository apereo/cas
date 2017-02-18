package org.apereo.cas.support.pac4j.config;

import org.apereo.cas.CentralAuthenticationService;
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
import org.apereo.cas.support.pac4j.web.flow.ClientAction;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.pac4j.config.client.PropertiesConfigFactory;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(Pac4jConfiguration.class);
    
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired(required = false)
    @Qualifier("clientPrincipalResolver")
    private PrincipalResolver clientPrincipalResolver;

    @Autowired(required = false)
    @Qualifier("indirectClients")
    private IndirectClient[] clients;

    @Autowired
    @Qualifier("authenticationHandlersResolvers")
    private Map authenticationHandlersResolvers;

    @Autowired
    @Qualifier("authenticationMetadataPopulators")
    private List authenticationMetadataPopulators;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Bean
    public PrincipalFactory clientPrincipalFactory() {
        return new DefaultPrincipalFactory();
    }

    @Bean
    public Pac4jProperties pac4jProperties() {
        return new Pac4jProperties();
    }

    @Bean
    public AuthenticationMetaDataPopulator clientAuthenticationMetaDataPopulator() {
        return new ClientAuthenticationMetaDataPopulator();
    }

    @Bean
    public ClientAuthenticationHandler clientAuthenticationHandler() {
        final ClientAuthenticationHandler h = new ClientAuthenticationHandler();
        h.setClients(builtClients());
        h.setPrincipalFactory(clientPrincipalFactory());
        h.setServicesManager(servicesManager);
        h.setTypedIdUsed(casProperties.getAuthn().getPac4j().isTypedIdUsed());
        return h;
    }

    @Bean
    public Action clientAction() {
        final ClientAction a = new ClientAction(casProperties);
        a.setCentralAuthenticationService(centralAuthenticationService);
        a.setAuthenticationSystemSupport(authenticationSystemSupport);
        a.setClients(builtClients());
        return a;
    }

    private void configureGithubClient(final Map<String, String> properties) {
        properties.put(PropertiesConfigFactory.GITHUB_ID, casProperties.getAuthn().getPac4j().getGithub().getId());
        properties.put(PropertiesConfigFactory.GITHUB_SECRET, casProperties.getAuthn().getPac4j().getGithub().getSecret());
    }

    private void configureDropboxClient(final Map<String, String> properties) {
        properties.put(PropertiesConfigFactory.DROPBOX_ID, casProperties.getAuthn().getPac4j().getDropbox().getId());
        properties.put(PropertiesConfigFactory.DROPBOX_SECRET, casProperties.getAuthn().getPac4j().getDropbox().getSecret());
    }

    private void configureWindowsLiveClient(final Map<String, String> properties) {
        properties.put(PropertiesConfigFactory.WINDOWSLIVE_ID, casProperties.getAuthn().getPac4j().getWindowsLive().getId());
        properties.put(PropertiesConfigFactory.WINDOWSLIVE_SECRET, casProperties.getAuthn().getPac4j().getWindowsLive().getSecret());
    }

    private void configureYahooClient(final Map<String, String> properties) {
        properties.put(PropertiesConfigFactory.YAHOO_ID, casProperties.getAuthn().getPac4j().getYahoo().getId());
        properties.put(PropertiesConfigFactory.YAHOO_SECRET, casProperties.getAuthn().getPac4j().getYahoo().getSecret());
    }

    private void configureFoursquareClient(final Map<String, String> properties) {
        properties.put(PropertiesConfigFactory.FOURSQUARE_ID, casProperties.getAuthn().getPac4j().getFoursquare().getId());
        properties.put(PropertiesConfigFactory.FOURSQUARE_SECRET, casProperties.getAuthn().getPac4j().getFoursquare().getSecret());
    }

    private void configureGoogleClient(final Map<String, String> properties) {
        properties.put(PropertiesConfigFactory.GOOGLE_ID, casProperties.getAuthn().getPac4j().getGoogle().getId());
        properties.put(PropertiesConfigFactory.GOOGLE_SECRET, casProperties.getAuthn().getPac4j().getGoogle().getSecret());
        properties.put(PropertiesConfigFactory.GOOGLE_SCOPE, casProperties.getAuthn().getPac4j().getGoogle().getScope());
    }

    private void configureFacebookClient(final Map<String, String> properties) {
        properties.put(PropertiesConfigFactory.FACEBOOK_ID, casProperties.getAuthn().getPac4j().getFacebook().getId());
        properties.put(PropertiesConfigFactory.FACEBOOK_SECRET, casProperties.getAuthn().getPac4j().getFacebook().getSecret());
        properties.put(PropertiesConfigFactory.FACEBOOK_SCOPE, casProperties.getAuthn().getPac4j().getFacebook().getScope());
        properties.put(PropertiesConfigFactory.FACEBOOK_FIELDS, casProperties.getAuthn().getPac4j().getFacebook().getFields());
    }

    private void configureTwitterClient(final Map<String, String> properties) {
        properties.put(PropertiesConfigFactory.TWITTER_ID, casProperties.getAuthn().getPac4j().getTwitter().getId());
        properties.put(PropertiesConfigFactory.TWITTER_SECRET, casProperties.getAuthn().getPac4j().getTwitter().getSecret());
    }

    private void configureCasClient(final Map<String, String> properties) {
        properties.put(PropertiesConfigFactory.CAS_LOGIN_URL, casProperties.getAuthn().getPac4j().getCas().getLoginUrl());
        properties.put(PropertiesConfigFactory.CAS_PROTOCOL, casProperties.getAuthn().getPac4j().getCas().getProtocol());
    }

    private void configureSamlClient(final Map<String, String> properties) {
        properties.put(PropertiesConfigFactory.SAML_IDENTITY_PROVIDER_METADATA_PATH,
                casProperties.getAuthn().getPac4j().getSaml().getIdentityProviderMetadataPath());
        properties.put(PropertiesConfigFactory.SAML_KEYSTORE_PASSWORD,
                casProperties.getAuthn().getPac4j().getSaml().getKeystorePassword());
        properties.put(PropertiesConfigFactory.SAML_KEYSTORE_PATH,
                casProperties.getAuthn().getPac4j().getSaml().getKeystorePath());
        properties.put(PropertiesConfigFactory.SAML_MAXIMUM_AUTHENTICATION_LIFETIME,
                casProperties.getAuthn().getPac4j().getSaml().getMaximumAuthenticationLifetime());
        properties.put(PropertiesConfigFactory.SAML_PRIVATE_KEY_PASSWORD,
                casProperties.getAuthn().getPac4j().getSaml().getPrivateKeyPassword());
        properties.put(PropertiesConfigFactory.SAML_SERVICE_PROVIDER_ENTITY_ID,
                casProperties.getAuthn().getPac4j().getSaml().getServiceProviderEntityId());
        properties.put(PropertiesConfigFactory.SAML_SERVICE_PROVIDER_METADATA_PATH,
                casProperties.getAuthn().getPac4j().getSaml().getServiceProviderMetadataPath());
        properties.put(PropertiesConfigFactory.SAML_DESTINATION_BINDING_TYPE, SAMLConstants.SAML2_REDIRECT_BINDING_URI);
    }

    private void configureOidcClient(final Map<String, String> properties) {
        properties.put(PropertiesConfigFactory.OIDC_CUSTOM_PARAM_KEY1,
                casProperties.getAuthn().getPac4j().getOidc().getCustomParamKey1());
        properties.put(PropertiesConfigFactory.OIDC_CUSTOM_PARAM_KEY2,
                casProperties.getAuthn().getPac4j().getOidc().getCustomParamKey2());
        properties.put(PropertiesConfigFactory.OIDC_CUSTOM_PARAM_VALUE1,
                casProperties.getAuthn().getPac4j().getOidc().getCustomParamValue1());
        properties.put(PropertiesConfigFactory.OIDC_CUSTOM_PARAM_VALUE2,
                casProperties.getAuthn().getPac4j().getOidc().getCustomParamValue2());
        properties.put(PropertiesConfigFactory.OIDC_DISCOVERY_URI,
                casProperties.getAuthn().getPac4j().getOidc().getDiscoveryUri());
        properties.put(PropertiesConfigFactory.OIDC_ID,
                casProperties.getAuthn().getPac4j().getOidc().getId());
        properties.put(PropertiesConfigFactory.OIDC_MAX_CLOCK_SKEW,
                casProperties.getAuthn().getPac4j().getOidc().getMaxClockSkew());
        properties.put(PropertiesConfigFactory.OIDC_PREFERRED_JWS_ALGORITHM,
                casProperties.getAuthn().getPac4j().getOidc().getPreferredJwsAlgorithm());
        properties.put(PropertiesConfigFactory.OIDC_SECRET, casProperties.getAuthn().getPac4j().getOidc().getSecret());
        properties.put(PropertiesConfigFactory.OIDC_USE_NONCE, casProperties.getAuthn().getPac4j().getOidc().getUseNonce());
    }

    private void configureLinkedInClient(final Map<String, String> properties) {
        properties.put(PropertiesConfigFactory.LINKEDIN_ID, casProperties.getAuthn().getPac4j().getLinkedIn().getId());
        properties.put(PropertiesConfigFactory.LINKEDIN_SECRET, casProperties.getAuthn().getPac4j().getLinkedIn().getSecret());
        properties.put(PropertiesConfigFactory.LINKEDIN_SCOPE, casProperties.getAuthn().getPac4j().getLinkedIn().getScope());
        properties.put(PropertiesConfigFactory.LINKEDIN_FIELDS, casProperties.getAuthn().getPac4j().getLinkedIn().getFields());
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

        // add all indirect clients from the Spring context
        if (this.clients != null && this.clients.length > 0) {
            allClients.addAll(Arrays.<Client>asList(this.clients));
        } 
        
        if (allClients.isEmpty()) {
            LOGGER.debug("No pac4j clients are defined");
        }
        return new Clients(casProperties.getServer().getLoginUrl(), allClients);
    }

    @PostConstruct
    protected void initializeRootApplicationContext() {
        final ClientAuthenticationHandler handler = clientAuthenticationHandler();
        if (!handler.getClients().findAllClients().isEmpty()) {
            authenticationHandlersResolvers.put(clientAuthenticationHandler(), this.clientPrincipalResolver);
            authenticationMetadataPopulators.add(0, clientAuthenticationMetaDataPopulator());
        }
    }
}
