package org.apereo.cas.services;

import org.apereo.cas.config.CasCoreNotificationsConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.config.JpaServiceRegistryConfiguration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * This is {@link CustomTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    AopAutoConfiguration.class,
//    JpaServiceRegistryTests.JpaServiceRegistryTestConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreNotificationsConfiguration.class,
    JpaServiceRegistryConfiguration.class,
    CasHibernateJpaConfiguration.class,
    CasCoreServicesConfiguration.class
}, properties = {
    "spring.jpa.properties.javax.persistence.validation.mode=none",
    "cas.service-registry.schedule.enabled=false",
    "cas.service-registry.jpa.fetch-size=3000",
    "cas.service-registry.jpa.batch-size=500",
    "cas.service-registry.jpa.generate-statistics=false",
    "cas.service-registry.jpa.user=postgres",
    "cas.service-registry.jpa.password=password",
    "cas.service-registry.jpa.driver-class=org.postgresql.Driver",
    "cas.service-registry.jpa.url=jdbc:postgresql://localhost:5432/services",
    "cas.service-registry.jpa.dialect=org.hibernate.dialect.PostgreSQL95Dialect"
})
public class CustomTests {
    @Autowired
    @Qualifier("jpaServiceRegistry")
    private ServiceRegistry newServiceRegistry;
    
    @Test
    public void verifyOperation() {
        for (int i = 0; i < 1000; i++) {
            var svc = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), true);
            newServiceRegistry.save(svc);
            System.out.println(i);
        }
        System.out.println(newServiceRegistry.size());
//        System.out.println(newServiceRegistry.load().size());
    }
}

