package org.apereo.cas.consent;

import module java.base;
import org.apereo.cas.config.CasConsentJdbcAutoConfiguration;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasHibernateJpaAutoConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TenantJdbcConsentRepositoryBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@SpringBootTest(classes = {
    CasCoreMultitenancyAutoConfiguration.class,
    CasConsentJdbcAutoConfiguration.class,
    CasHibernateJpaAutoConfiguration.class,
    BaseConsentRepositoryTests.SharedTestConfiguration.class
}, properties = {
    "cas.consent.jpa.enabled=false",
    
    "cas.multitenancy.core.enabled=true",
    "cas.multitenancy.json.location=classpath:/tenants.json",

    "cas.jdbc.show-sql=false",
    "cas.consent.jpa.ddl-auto=create-drop"
})
@EnabledIfListeningOnPort(port = 5432)
@Tag("Postgres")
@ExtendWith(CasTestExtension.class)
class TenantJdbcConsentRepositoryBuilderTests {
    @Autowired
    @Qualifier(ConsentEngine.BEAN_NAME)
    private ConsentEngine consentEngine;
    
    @Test
    void verifyOperation() throws Throwable {
        val principal = RegisteredServiceTestUtils.getPrincipal(UUID.randomUUID().toString(),
            CollectionUtils.wrap("email", List.of("casuser@example.org")));
        val authentication = RegisteredServiceTestUtils.getAuthentication(principal);

        val service = RegisteredServiceTestUtils.getService();
        service.setTenant("shire");

        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        registeredService.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());

        val storedDecision = consentEngine.storeConsentDecision(
            service,
            registeredService,
            authentication,
            1, ChronoUnit.DAYS, ConsentReminderOptions.ATTRIBUTE_NAME);
        assertNotNull(storedDecision);

        val foundDecision = consentEngine.findConsentDecision(service, registeredService, authentication);
        assertEquals(storedDecision, foundDecision);
    }
}
