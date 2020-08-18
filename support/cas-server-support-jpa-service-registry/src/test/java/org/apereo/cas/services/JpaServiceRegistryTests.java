package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.config.JpaServiceRegistryConfiguration;
import org.apereo.cas.jpa.JpaPersistenceProviderConfigurer;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

/**
 * Handles tests for {@link JpaServiceRegistry}
 *
 * @author battags
 * @since 3.1.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    AopAutoConfiguration.class,
    JpaServiceRegistryTests.JpaServiceRegistryTestConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    JpaServiceRegistryConfiguration.class,
    CasHibernateJpaConfiguration.class,
    CasCoreServicesConfiguration.class
}, properties = "cas.jdbc.show-sql=false")
@Tag("JDBC")
@DirtiesContext
@Getter
public class JpaServiceRegistryTests extends AbstractServiceRegistryTests {

    @Autowired
    @Qualifier("jpaServiceRegistry")
    protected ServiceRegistry newServiceRegistry;

    @TestConfiguration("JpaServiceRegistryTestConfiguration")
    public static class JpaServiceRegistryTestConfiguration {
        @Bean
        public JpaPersistenceProviderConfigurer jpaServicePersistenceProviderTestConfigurer() {
            return context -> context.getIncludeEntityClasses().addAll(List.of(
                SamlRegisteredService.class.getName(),
                WSFederationRegisteredService.class.getName(),
                OidcRegisteredService.class.getName(),
                OAuthRegisteredService.class.getName()));
        }
    }
}
