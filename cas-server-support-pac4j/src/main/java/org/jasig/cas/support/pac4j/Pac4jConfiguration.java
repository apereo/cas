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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Initializes the pac4j configuration.
 *
 * @author Jerome Leleu
 * @since 4.2.0
 */
@Configuration
public class Pac4jConfiguration {

    private static final String CAS_PAC4J_PREFIX = "cas.pac4j.";

    @Value("${server.prefix:http://localhost:8080/cas}/login")
    private String serverLoginUrl;

    @Autowired(required = true)
    @Qualifier("casProperties")
    private Properties casProperties;

    @Autowired(required = false)
    private IndirectClient[] clients;

    /**
     * Returning the built clients.
     *
     * @return the built clients.
     */
    @Bean(name = "builtClients")
    public Clients clients() {
        final List<Client> allClients = new ArrayList<>();

        // turn the properties file into a map of properties
        final Map<String, String> properties = new HashMap<>();
        final Enumeration names = casProperties.propertyNames();
        while (names.hasMoreElements()) {
            final String name = (String) names.nextElement();
            if (name.startsWith(CAS_PAC4J_PREFIX)) {
                properties.put(name.substring(CAS_PAC4J_PREFIX.length()), casProperties.getProperty(name));
            }
        }
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
