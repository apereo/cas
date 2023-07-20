package org.apereo.cas.support.saml.services;

import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.util.model.TriStateBoolean;

import lombok.val;
import org.cryptacular.io.ClassPathResource;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlRegisteredServiceSerializationCustomizerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("SAML2")
class SamlRegisteredServiceSerializationCustomizerTests {

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    class NoDefaults extends BaseSamlIdPConfigurationTests {
        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Test
        void verifyNoDefaults() throws Exception {
            val serializer = new RegisteredServiceJsonSerializer(applicationContext);
            val service = (SamlRegisteredService) serializer.from(new ClassPathResource("services/SampleSAML-1000.json").getInputStream());
            assertNotNull(service);
            assertEquals(TriStateBoolean.FALSE, service.getSignAssertions());
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = "cas.authn.saml-idp.services.defaults.signAssertions=true")
    class WithDefaults extends BaseSamlIdPConfigurationTests {
        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Test
        void verifyDefaults() {
            val serializer = new RegisteredServiceJsonSerializer(applicationContext);
            val service = (SamlRegisteredService) serializer.from(new ClassPathResource("services/SampleSAML-1000.json").getInputStream());
            assertNotNull(service);
            assertEquals(TriStateBoolean.TRUE, service.getSignAssertions());
        }

        @Test
        void verifyDefaultsOverriddenByService() {
            val serializer = new RegisteredServiceJsonSerializer(applicationContext);
            val service = (SamlRegisteredService) serializer.from(new ClassPathResource("services/SampleSAML-1001.json").getInputStream());
            assertNotNull(service);
            assertEquals(TriStateBoolean.UNDEFINED, service.getSignAssertions());
        }
    }
}
