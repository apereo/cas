package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jdbc.JdbcAuthenticationProperties;
import org.apereo.cas.configuration.model.support.ldap.LdapAuthenticationProperties;
import org.apereo.cas.discovery.CasServerProfileRegistrar;
import org.apereo.cas.services.ServicesManager;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.pac4j.core.client.Clients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
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
@Slf4j
public class CasDiscoveryProfileConfiguration {

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    @Qualifier("builtClients")
    private Clients builtClients;

    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;

    @Bean
    public CasServerProfileRegistrar casServerProfileRegistrar() {
        return new CasServerProfileRegistrar(this.servicesManager, casProperties, this.builtClients, availableAttributes());
    }

    @Bean
    public Set<String> availableAttributes() {
        final LinkedHashSet<String> attributes = new LinkedHashSet<>(0);
        attributes.addAll(attributeRepository.getPossibleUserAttributeNames());

        final List<LdapAuthenticationProperties> ldapProps = casProperties.getAuthn().getLdap();
        if (ldapProps != null) {
            ldapProps.stream()
                .forEach(ldap -> {
                    attributes.addAll(transformAttributes(ldap.getPrincipalAttributeList()));
                    attributes.addAll(transformAttributes(ldap.getAdditionalAttributes()));
                });
        }
        final JdbcAuthenticationProperties jdbcProps = casProperties.getAuthn().getJdbc();
        if (jdbcProps != null) {
            jdbcProps.getQuery().stream()
                .forEach(jdbc -> attributes.addAll(transformAttributes(jdbc.getPrincipalAttributeList())));
        }
        return attributes;
    }

    private Set<String> transformAttributes(final List<String> attributes) {
        final Set<String> attributeSet = new LinkedHashSet<>();
        CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(attributes)
            .values()
            .stream()
            .forEach(v -> attributeSet.add(v.toString()));
        return attributeSet;
    }
}
