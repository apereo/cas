package org.apereo.cas.config;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.discovery.CasServerDiscoveryProfileEndpoint;
import org.apereo.cas.discovery.CasServerProfileRegistrar;
import org.apereo.cas.services.ServicesManager;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.pac4j.core.client.Clients;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This is {@link CasDiscoveryProfileConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("casDiscoveryProfileConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasDiscoveryProfileConfiguration {

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("builtClients")
    private ObjectProvider<Clients> builtClients;

    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;

    @Bean
    public CasServerProfileRegistrar casServerProfileRegistrar() {
        return new CasServerProfileRegistrar(this.servicesManager, casProperties,
            this.builtClients.getIfAvailable(),
            availableAttributes());
    }

    @Bean
    @ConditionalOnEnabledEndpoint
    public CasServerDiscoveryProfileEndpoint discoveryProfileEndpoint() {
        return new CasServerDiscoveryProfileEndpoint(casProperties, servicesManager, casServerProfileRegistrar());
    }

    @Bean
    public Set<String> availableAttributes() {
        val attributes = new LinkedHashSet<String>(0);
        val possibleUserAttributeNames = attributeRepository.getPossibleUserAttributeNames();
        if (possibleUserAttributeNames != null) {
            attributes.addAll(possibleUserAttributeNames);
        }

        val ldapProps = casProperties.getAuthn().getLdap();
        if (ldapProps != null) {
            ldapProps.forEach(ldap -> {
                attributes.addAll(transformAttributes(ldap.getPrincipalAttributeList()));
                attributes.addAll(transformAttributes(ldap.getAdditionalAttributes()));
            });
        }
        val jdbcProps = casProperties.getAuthn().getJdbc();
        if (jdbcProps != null) {
            jdbcProps.getQuery()
                .forEach(jdbc -> attributes.addAll(transformAttributes(jdbc.getPrincipalAttributeList())));
        }
        return attributes;
    }

    private Set<String> transformAttributes(final List<String> attributes) {
        val attributeSet = new LinkedHashSet<String>();
        CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(attributes)
            .values()
            .forEach(v -> attributeSet.add(v.toString()));
        return attributeSet;
    }
}
