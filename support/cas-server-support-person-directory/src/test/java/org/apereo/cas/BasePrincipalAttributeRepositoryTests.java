package org.apereo.cas;

import org.apereo.cas.authentication.attribute.AttributeDefinitionStore;
import org.apereo.cas.authentication.attribute.DefaultAttributeDefinitionStore;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.config.CasPersonDirectoryGroovyConfiguration;
import org.apereo.cas.config.CasPersonDirectoryJdbcConfiguration;
import org.apereo.cas.config.CasPersonDirectoryJsonConfiguration;
import org.apereo.cas.config.CasPersonDirectoryLdapConfiguration;
import org.apereo.cas.config.CasPersonDirectoryRestConfiguration;
import org.apereo.cas.config.CasPersonDirectoryStubConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import lombok.val;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * This is {@link BasePrincipalAttributeRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public abstract class BasePrincipalAttributeRepositoryTests {
    @ImportAutoConfiguration({
        MailSenderAutoConfiguration.class,
        AopAutoConfiguration.class,
        RefreshAutoConfiguration.class,
        WebMvcAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        CasPersonDirectoryConfiguration.class,
        CasPersonDirectoryJdbcConfiguration.class,
        CasPersonDirectoryLdapConfiguration.class,
        CasPersonDirectoryGroovyConfiguration.class,
        CasPersonDirectoryRestConfiguration.class,
        CasPersonDirectoryJsonConfiguration.class,
        CasPersonDirectoryStubConfiguration.class,
        CasCoreUtilConfiguration.class,
        CasCoreNotificationsConfiguration.class,
        CasCoreWebConfiguration.class,
        CasCoreHttpConfiguration.class,
        CasCoreServicesConfiguration.class,

        SharedTestConfiguration.AttributeDefinitionsTestConfiguration.class
    })
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
