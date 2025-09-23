package org.apereo.cas;

import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.DefaultAttributeDefinitionStore;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreEnvironmentBootstrapAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BasePrincipalAttributeRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public abstract class BasePrincipalAttributeRepositoryTests {
    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration({
        CasPersonDirectoryAutoConfiguration.class,
        CasCoreEnvironmentBootstrapAutoConfiguration.class,
        CasCoreMultitenancyAutoConfiguration.class,
        CasCoreUtilAutoConfiguration.class,
        CasCoreNotificationsAutoConfiguration.class,
        CasCoreWebAutoConfiguration.class,
        CasCoreAutoConfiguration.class,
        CasCoreServicesAutoConfiguration.class,
        CasCoreAuthenticationAutoConfiguration.class,
        CasCoreTicketsAutoConfiguration.class,
        CasCoreLogoutAutoConfiguration.class,
        CasCoreScriptingAutoConfiguration.class
    })
    @SpringBootConfiguration(proxyBeanMethods = false)
    @Import(SharedTestConfiguration.AttributeDefinitionsTestConfiguration.class)
    public static class SharedTestConfiguration {

        @TestConfiguration(value = "AttributeDefinitionsTestConfiguration", proxyBeanMethods = false)
        public static class AttributeDefinitionsTestConfiguration {
            @Bean
            public AttributeDefinitionStore attributeDefinitionStore(
                final CasConfigurationProperties casProperties) throws Exception {
                val resource = casProperties.getAuthn().getAttributeRepository()
                    .getAttributeDefinitionStore().getJson().getLocation();
                val store = new DefaultAttributeDefinitionStore(resource);
                store.setScope(casProperties.getServer().getScope());
                return store;
            }
        }
    }
}
