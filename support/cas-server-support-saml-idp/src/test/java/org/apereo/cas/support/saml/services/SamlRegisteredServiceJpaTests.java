package org.apereo.cas.support.saml.services;

import org.apereo.cas.config.JpaServiceRegistryConfiguration;
import org.apereo.cas.services.ChainingAttributeReleasePolicy;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DenyAllAttributeReleasePolicy;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * The {@link SamlRegisteredServiceJpaTests} handles test cases for {@link SamlRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Import(JpaServiceRegistryConfiguration.class)
public class SamlRegisteredServiceJpaTests extends BaseSamlIdPConfigurationTests {

    @BeforeEach
    public void before() {
        this.servicesManager.deleteAll();
    }

    @Test
    public void verifySavingSamlService() {
        val service = new SamlRegisteredService();
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
        servicesManager.save(service);
        servicesManager.load();
        val services = servicesManager.getAllServices();
        assertEquals(2, services.size());
        services.forEach(s -> servicesManager.delete(s.getId()));
        assertEquals(0, servicesManager.count());
    }
}
