package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.discovery.CasServerDiscoveryProfileEndpoint;
import org.apereo.cas.discovery.CasServerProfileRegistrar;
import org.apereo.cas.util.spring.BeanContainer;

import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.pac4j.core.client.Clients;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
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
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration(value = "casDiscoveryProfileConfiguration", proxyBeanMethods = false)
public class CasDiscoveryProfileConfiguration {

    private static Set<String> transformAttributes(final List<String> attributes) {
        val attributeSet = new LinkedHashSet<String>(attributes.size());
        CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(attributes).values().forEach(v -> attributeSet.add(v.toString()));
        return attributeSet;
    }

    @Bean
    @Autowired
    public CasServerProfileRegistrar casServerProfileRegistrar(
        final CasConfigurationProperties casProperties,
        @Qualifier("builtClients")
        final ObjectProvider<Clients> builtClients,
        @Qualifier("discoveryProfileAvailableAttributes")
        final BeanContainer<String> discoveryProfileAvailableAttributes,
        @Qualifier("authenticationEventExecutionPlan")
        final AuthenticationEventExecutionPlan authenticationEventExecutionPlan) {
        return new CasServerProfileRegistrar(casProperties, builtClients.getIfAvailable(),
            discoveryProfileAvailableAttributes.toSet(), authenticationEventExecutionPlan);
    }

    @Bean
    @ConditionalOnAvailableEndpoint
    @Autowired
    public CasServerDiscoveryProfileEndpoint discoveryProfileEndpoint(
        final CasConfigurationProperties casProperties,
        @Qualifier("casServerProfileRegistrar")
        final CasServerProfileRegistrar casServerProfileRegistrar) {
        return new CasServerDiscoveryProfileEndpoint(casProperties, casServerProfileRegistrar);
    }

    @Bean
    @Autowired
    public BeanContainer<String> discoveryProfileAvailableAttributes(
        final CasConfigurationProperties casProperties,
        @Qualifier("attributeRepository")
        final IPersonAttributeDao attributeRepository) {
        val attributes = new LinkedHashSet<String>(0);
        val possibleUserAttributeNames = attributeRepository.getPossibleUserAttributeNames(IPersonAttributeDaoFilter.alwaysChoose());
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
            jdbcProps.getQuery().forEach(jdbc -> attributes.addAll(transformAttributes(jdbc.getPrincipalAttributeList())));
        }
        return BeanContainer.of(attributes);
    }
}
