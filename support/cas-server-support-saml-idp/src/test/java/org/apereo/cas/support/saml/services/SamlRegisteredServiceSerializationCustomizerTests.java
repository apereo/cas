package org.apereo.cas.support.saml.services;

import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
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
    class NoDefaults extends BaseSamlIdPConfigurationTests {
        @Test
        void verifyNoDefaults() throws Throwable {
            val serializer = new RegisteredServiceJsonSerializer(applicationContext);
            val service = (SamlRegisteredService) serializer.from(new ClassPathResource("services/SampleSAML-1000.json").getInputStream());
            assertNotNull(service);
            assertEquals(TriStateBoolean.FALSE, service.getSignAssertions());
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.saml-idp.services.defaults.signAssertions=true")
    class WithDefaults extends BaseSamlIdPConfigurationTests {
        @Test
        void verifyDefaults() throws Throwable {
            val serializer = new RegisteredServiceJsonSerializer(applicationContext);
            val service = (SamlRegisteredService) serializer.from(new ClassPathResource("services/SampleSAML-1000.json").getInputStream());
            assertNotNull(service);
            assertEquals(TriStateBoolean.TRUE, service.getSignAssertions());
        }

        @Test
        void verifyDefaultsOverriddenByService() throws Throwable {
            val serializer = new RegisteredServiceJsonSerializer(applicationContext);
            val service = (SamlRegisteredService) serializer.from(new ClassPathResource("services/SampleSAML-1001.json").getInputStream());
            assertNotNull(service);
            assertEquals(TriStateBoolean.UNDEFINED, service.getSignAssertions());
        }
    }
}
