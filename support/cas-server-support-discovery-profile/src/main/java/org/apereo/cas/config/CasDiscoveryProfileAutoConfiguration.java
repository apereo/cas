package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.discovery.CasServerDiscoveryProfileEndpoint;
import org.apereo.cas.discovery.CasServerProfileRegistrar;
import org.apereo.cas.discovery.DefaultCasServerProfileRegistrar;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This is {@link CasDiscoveryProfileAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Discovery)
@AutoConfiguration
public class CasDiscoveryProfileAutoConfiguration {

    @Configuration(value = "DiscoveryProfileCoreConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DiscoveryProfileCoreConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = CasServerProfileRegistrar.BEAN_NAME)
        public CasServerProfileRegistrar casServerProfileRegistrar(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("discoveryProfileAvailableAttributes") final BeanContainer<String> discoveryProfileAvailableAttributes,
            @Qualifier("authenticationEventExecutionPlan") final AuthenticationEventExecutionPlan authenticationEventExecutionPlan) {
            return new DefaultCasServerProfileRegistrar(casProperties, discoveryProfileAvailableAttributes.toSet(),
                authenticationEventExecutionPlan, applicationContext);
        }
    }

    @Configuration(value = "DiscoveryProfileWebConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DiscoveryProfileWebConfiguration {
        @Bean
        @ConditionalOnAvailableEndpoint
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CasServerDiscoveryProfileEndpoint discoveryProfileEndpoint(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(CasServerProfileRegistrar.BEAN_NAME)
            final ObjectProvider<CasServerProfileRegistrar> casServerProfileRegistrar) {
            return new CasServerDiscoveryProfileEndpoint(casProperties, applicationContext, casServerProfileRegistrar);
        }
    }

    @Configuration(value = "DiscoveryProfileAttributesConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DiscoveryProfileAttributesConfiguration {
        private static Set<String> transformAttributes(final List<String> attributes) {
            val attributeSet = new LinkedHashSet<String>(attributes.size());
            CoreAuthenticationUtils.transformPrincipalAttributesListIntoMultiMap(attributes).values().forEach(v -> attributeSet.add(v.toString()));
            return attributeSet;
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "discoveryProfileAvailableAttributes")
        public BeanContainer<String> discoveryProfileAvailableAttributes(
            final CasConfigurationProperties casProperties,
            @Qualifier(PrincipalResolver.BEAN_NAME_ATTRIBUTE_REPOSITORY) final ObjectProvider<PersonAttributeDao> attributeRepository) {

            val attributes = new LinkedHashSet<String>();
            attributeRepository.ifAvailable(repository -> {
                val possibleUserAttributeNames = repository.getPossibleUserAttributeNames(PersonAttributeDaoFilter.alwaysChoose());
                if (possibleUserAttributeNames != null) {
                    attributes.addAll(possibleUserAttributeNames);
                }
            });

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
}
