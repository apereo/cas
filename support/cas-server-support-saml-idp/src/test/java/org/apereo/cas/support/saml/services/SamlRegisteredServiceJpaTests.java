package org.apereo.cas.support.saml.services;

import org.apereo.cas.config.CasHibernateJpaConfiguration;
import org.apereo.cas.config.JpaServiceRegistryConfiguration;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The {@link SamlRegisteredServiceJpaTests} handles test cases for {@link SamlRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Import({
    CasHibernateJpaConfiguration.class,
    JpaServiceRegistryConfiguration.class
})
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.service-registry.jpa.ddl-auto=create-drop",
    "cas.service-registry.jpa.url=jdbc:hsqldb:mem:cas-services-${#randomString6}"
})
public class SamlRegisteredServiceJpaTests extends BaseSamlIdPConfigurationTests {

    @BeforeEach
    public void before() {
        this.servicesManager.deleteAll();
    }

    @Test
    public void verifySavingSamlService() {
        var service = new SamlRegisteredService();
        service.setName("SAML");
        service.setServiceId("http://mmoayyed.example.net");
        service.setMetadataLocation("classpath:/metadata/idp-metadata.xml");
        val policy = new InCommonRSAttributeReleasePolicy();
        val chain = new ChainingAttributeReleasePolicy();
        chain.setPolicies(Arrays.asList(policy, new DenyAllAttributeReleasePolicy()));
        service.setAttributeReleasePolicy(chain);
        service.setDescription("Description");
        service.setAttributeNameFormats(CollectionUtils.wrap("key", "value"));
        service.setAttributeFriendlyNames(CollectionUtils.wrap("friendly-name", "value"));
        service.setAccessStrategy(new DefaultRegisteredServiceAccessStrategy(true, true));
        service = (SamlRegisteredService) servicesManager.save(service);
        val services = servicesManager.load();
        service = servicesManager.findServiceBy(service.getId(), SamlRegisteredService.class);
        assertNotNull(service);
        services.forEach(s -> servicesManager.delete(s.getId()));
        assertEquals(0, servicesManager.count());
    }
}
