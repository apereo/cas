package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.entity.SamlIdentityProviderEntity;
import org.apereo.cas.entity.SamlIdentityProviderEntityParser;
import org.apereo.cas.web.SamlIdentityProviderDiscoveryFeedController;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.client.Clients;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link SamlIdentityProviderDiscoveryConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration(value = "samlIdentityProviderDiscoveryConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class SamlIdentityProviderDiscoveryConfiguration {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("builtClients")
    private ObjectProvider<Clients> builtClients;

    @Bean
    public SamlIdentityProviderDiscoveryFeedController identityProviderDiscoveryFeedController() {
        return new SamlIdentityProviderDiscoveryFeedController(samlIdentityProviderEntityParser());
    }

    @Bean
    public List<SamlIdentityProviderEntityParser> samlIdentityProviderEntityParser() {
        val parsers = new ArrayList<SamlIdentityProviderEntityParser>();

        val resource = casProperties.getAuthn().getSamlIdp().getDiscovery().getResource();
        resource
            .stream()
            .filter(res -> res.getLocation() != null)
            .forEach(Unchecked.consumer(res -> parsers.add(new SamlIdentityProviderEntityParser(res.getLocation()))));

        builtClients.ifAvailable(clients -> {
            clients.findAllClients()
                .stream()
                .filter(c -> c instanceof SAML2Client)
                .map(c -> SAML2Client.class.cast(c))
                .forEach(c -> {
                    c.init();
                    val entity = new SamlIdentityProviderEntity();
                    entity.setEntityID(c.getIdentityProviderResolvedEntityId());
                    parsers.add(new SamlIdentityProviderEntityParser(entity));
                });
        });
        return parsers;
    }
}
