package org.jasig.cas.support.pac4j;

import org.pac4j.config.client.PropertiesConfigFactory;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Initializes the pac4j configuration.
 *
 * @author Jerome Leleu
 * @since 4.2.0
 */
@Configuration("pac4jConfiguration")
public class Pac4jConfiguration {

    private static final String CAS_PAC4J_PREFIX = "cas.pac4j";

    @Value("${server.prefix:http://localhost:8080/cas}/login")
    private String serverLoginUrl;

    @Autowired(required = false)
    private IndirectClient[] clients;

    @Autowired
    @Qualifier("pac4jProperties")
    private Pac4jProperties pac4jProperties;
    
    /**
     * Returning the built clients.
     *
     * @return the built clients.
     */
    @RefreshScope
    @Bean(name = "builtClients")
    public Clients clients() {
        final List<Client> allClients = new ArrayList<>();

        // turn the properties file into a map of properties
        final Map<String, String> properties = new HashMap<>();
        
        properties.put(PropertiesConfigFactory.FACEBOOK_ID, pac4jProperties.getFacebook().getId());
        properties.put(PropertiesConfigFactory.FACEBOOK_SECRET, pac4jProperties.getFacebook().getSecret());
        properties.put(PropertiesConfigFactory.FACEBOOK_SCOPE, pac4jProperties.getFacebook().getScope());
        properties.put(PropertiesConfigFactory.FACEBOOK_FIELDS, pac4jProperties.getFacebook().getFields());

        properties.put(PropertiesConfigFactory.TWITTER_ID, pac4jProperties.getTwitter().getId());
        properties.put(PropertiesConfigFactory.TWITTER_SECRET, pac4jProperties.getTwitter().getSecret());
        
        properties.put(PropertiesConfigFactory.CAS_LOGIN_URL, pac4jProperties.getCas().getLoginUrl());
        properties.put(PropertiesConfigFactory.CAS_PROTOCOL, pac4jProperties.getCas().getProtocol());

        properties.put(PropertiesConfigFactory.SAML_IDENTITY_PROVIDER_METADATA_PATH,
                pac4jProperties.getSaml().getIdentityProviderMetadataPath());
        properties.put(PropertiesConfigFactory.SAML_KEYSTORE_PASSWORD,
                pac4jProperties.getSaml().getKeystorePassword());
        properties.put(PropertiesConfigFactory.SAML_KEYSTORE_PATH,
                pac4jProperties.getSaml().getKeystorePath());
        properties.put(PropertiesConfigFactory.SAML_MAXIMUM_AUTHENTICATION_LIFETIME,
                pac4jProperties.getSaml().getMaximumAuthenticationLifetime());
        properties.put(PropertiesConfigFactory.SAML_PRIVATE_KEY_PASSWORD,
                pac4jProperties.getSaml().getPrivateKeyPassword());
        properties.put(PropertiesConfigFactory.SAML_SERVICE_PROVIDER_ENTITY_ID,
                pac4jProperties.getSaml().getServiceProviderEntityId());
        properties.put(PropertiesConfigFactory.SAML_SERVICE_PROVIDER_METADATA_PATH,
                pac4jProperties.getSaml().getServiceProviderEntityId());
        
        properties.put(PropertiesConfigFactory.OIDC_CUSTOM_PARAM_KEY1, pac4jProperties.getOidc().getCustomParamKey1());
        properties.put(PropertiesConfigFactory.OIDC_CUSTOM_PARAM_KEY2, pac4jProperties.getOidc().getCustomParamKey2());
        properties.put(PropertiesConfigFactory.OIDC_CUSTOM_PARAM_VALUE1, pac4jProperties.getOidc().getCustomParamValue1());
        properties.put(PropertiesConfigFactory.OIDC_CUSTOM_PARAM_VALUE2, pac4jProperties.getOidc().getCustomParamValue2());
        properties.put(PropertiesConfigFactory.OIDC_DISCOVERY_URI, pac4jProperties.getOidc().getDiscoveryUri());
        properties.put(PropertiesConfigFactory.OIDC_ID, pac4jProperties.getOidc().getId());
        properties.put(PropertiesConfigFactory.OIDC_MAX_CLOCK_SKEW, pac4jProperties.getOidc().getMaxClockSkew());
        properties.put(PropertiesConfigFactory.OIDC_PREFERRED_JWS_ALGORITHM, pac4jProperties.getOidc().getPreferredJwsAlgorithm());
        properties.put(PropertiesConfigFactory.OIDC_SECRET, pac4jProperties.getOidc().getSecret());
        properties.put(PropertiesConfigFactory.OIDC_USE_NONCE, pac4jProperties.getOidc().getUseNonce());
        
        // add the new clients found via properties first
        final ConfigFactory configFactory = new PropertiesConfigFactory(properties);
        final Config propertiesConfig = configFactory.build();
        allClients.addAll(propertiesConfig.getClients().getClients());

        // add all indirect clients from the Spring context
        if (clients != null && clients.length > 0) {
            allClients.addAll(Arrays.<Client>asList(clients));
        }

        // build a Clients configuration
        if (allClients.isEmpty()) {
            throw new IllegalArgumentException("At least one pac4j client must be defined");
        }
        return new Clients(serverLoginUrl, allClients);
    }
}
