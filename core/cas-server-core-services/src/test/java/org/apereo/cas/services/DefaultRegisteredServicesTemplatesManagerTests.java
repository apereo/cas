package org.apereo.cas.services;

import org.apereo.cas.config.BaseAutoConfigurationTests;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultRegisteredServicesTemplatesManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("RegisteredService")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseAutoConfigurationTests.SharedTestConfiguration.class,
    properties = "cas.service-registry.templates.directory.location=classpath:/service-templates")
@EnableConfigurationProperties(CasConfigurationProperties.class)
class DefaultRegisteredServicesTemplatesManagerTests {
    @Autowired
    @Qualifier(RegisteredServicesTemplatesManager.BEAN_NAME)
    private RegisteredServicesTemplatesManager registeredServicesTemplatesManager;

    @Test
    void verifyNoTemplateLocation() {
        val registeredService = new CasRegisteredService();
        registeredService.setId(1000);
        registeredService.setName("Unknown");
        registeredService.setTemplateName("Unknown");
        registeredService.setServiceId("https://app.example.org");

        val properties = new CasConfigurationProperties();
        properties.getServiceRegistry().getTemplates().getDirectory().setLocation(new ClassPathResource("unknown"));
        val result = registeredServicesTemplatesManager.apply(registeredService);
        assertEquals(result.getId(), registeredService.getId());
        assertNull(result.getDescription());
        val releasePolicy = (ReturnAllowedAttributeReleasePolicy) result.getAttributeReleasePolicy();
        assertTrue(releasePolicy.getAllowedAttributes().isEmpty());
    }

    @Test
    void verifyNoTemplateName() {
        val registeredService = new CasRegisteredService();
        registeredService.setId(1000);
        registeredService.setName("Unknown");
        registeredService.setServiceId("https://app.example.org");

        val result = registeredServicesTemplatesManager.apply(registeredService);

        assertEquals(result.getId(), registeredService.getId());
        val releasePolicy = (ReturnAllowedAttributeReleasePolicy) result.getAttributeReleasePolicy();
        assertTrue(releasePolicy.getAllowedAttributes().isEmpty());
    }

    @Test
    void verifyOperation() {
        val registeredService = new CasRegisteredService();
        registeredService.setName("CAS");
        registeredService.setTemplateName("ExampleTemplate");
        registeredService.setId(1000);
        registeredService.setDescription("CAS service definition for example application");
        registeredService.setServiceId("https://app.example.org");
        registeredService.setUsernameAttributeProvider(new PrincipalAttributeRegisteredServiceUsernameProvider("uid"));

        val result = (CasRegisteredService) registeredServicesTemplatesManager.apply(registeredService);

        assertEquals(result.getName(), registeredService.getName());
        assertEquals(result.getId(), registeredService.getId());
        assertEquals(result.getDescription(), registeredService.getDescription());
        assertEquals(result.getServiceId(), registeredService.getServiceId());
        assertEquals(result.getUsernameAttributeProvider(), registeredService.getUsernameAttributeProvider());

        val releasePolicy = (ReturnAllowedAttributeReleasePolicy) result.getAttributeReleasePolicy();
        assertEquals(List.of("email", "username"), releasePolicy.getAllowedAttributes());
        assertEquals(Set.of("email", "username"), releasePolicy.getConsentPolicy().getIncludeOnlyAttributes());
        assertEquals(2, result.getSupportedProtocols().size());
    }

    @Test
    void verifyTemplateInheritance() {
        val registeredService = new CasRegisteredService();
        registeredService.setName("CAS");
        registeredService.setTemplateName("Unknown,UsernameProviderTemplate,AttributeReleaseTemplate");
        registeredService.setId(1000);

        val result = registeredServicesTemplatesManager.apply(registeredService);

        assertEquals(result.getName(), registeredService.getName());
        assertEquals(result.getId(), registeredService.getId());
        val uidProvider = (PrincipalAttributeRegisteredServiceUsernameProvider) result.getUsernameAttributeProvider();
        assertEquals("email", uidProvider.getUsernameAttribute());

        val releasePolicy = (ReturnAllowedAttributeReleasePolicy) result.getAttributeReleasePolicy();
        assertEquals(List.of("email", "username"), releasePolicy.getAllowedAttributes());
    }

    @Test
    void verifyGroovyTemplates() {
        val registeredService = new CasRegisteredService();
        registeredService.setName("CAS");
        registeredService.setTemplateName("GroovyTemplate");
        registeredService.setId(1000);
        registeredService.getProperties().put("GivenDescription",
            new DefaultRegisteredServiceProperty("This is my description"));
        registeredService.getProperties().put("GivenUsernameAttribute",
            new DefaultRegisteredServiceProperty("email"));
        registeredService.getProperties().put("AllowedAttributes",
            new DefaultRegisteredServiceProperty("email", "username"));

        val result = registeredServicesTemplatesManager.apply(registeredService);

        assertEquals(result.getName(), registeredService.getName());
        assertEquals(result.getId(), registeredService.getId());
        val uidProvider = (PrincipalAttributeRegisteredServiceUsernameProvider) result.getUsernameAttributeProvider();
        assertEquals("email", uidProvider.getUsernameAttribute());

        val releasePolicy = (ReturnAllowedAttributeReleasePolicy) result.getAttributeReleasePolicy();
        assertEquals(List.of("email", "username"), releasePolicy.getAllowedAttributes());
    }
}
