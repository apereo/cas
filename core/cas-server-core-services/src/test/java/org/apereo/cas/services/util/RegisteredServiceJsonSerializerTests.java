package org.apereo.cas.services.util;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.AuthenticationDateRegisteredServiceSingleSignOnParticipationPolicy;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.ChainingRegisteredServiceSingleSignOnParticipationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceAcceptableUsagePolicy;
import org.apereo.cas.services.LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.StringSerializer;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import java.io.File;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceJsonSerializerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Tag("FileSystem")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
class RegisteredServiceJsonSerializerTests {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    private StringSerializer<RegisteredService> serializer;

    @BeforeEach
    void initialize() {
        this.serializer = new RegisteredServiceJsonSerializer(applicationContext);
    }
    
    @Test
    void verifyPrinter() {
        assertFalse(serializer.supports(new File("bad-file")));
        assertFalse(serializer.getContentTypes().isEmpty());
    }

    @Test
    void verifyWriter() {
        val writer = new StringWriter();
        serializer.to(writer, new CasRegisteredService());
        assertNotNull(serializer.from(writer));
    }

    @Test
    void verifyList() {
        val registeredService = new CasRegisteredService();
        registeredService.setName("CasService");
        val results = serializer.fromList(CollectionUtils.wrapList(registeredService));
        assertNotNull(serializer.fromList(results));
    }

    @Test
    void checkNullability() {
        val json = """
            {
                "@class" : "org.apereo.cas.services.CasRegisteredService",
                "serviceId" : "^https://xyz.*"
                /* This is a comment here */
                "name" : "XYZ"
                "id" : "20161214",
            }""".indent(4);

        val registeredService = (CasRegisteredService) serializer.from(json);
        assertNotNull(registeredService);
        assertNotNull(registeredService.getAccessStrategy());
        assertNotNull(registeredService.getAttributeReleasePolicy());
        assertNotNull(registeredService.getProxyPolicy());
        assertNotNull(registeredService.getUsernameAttributeProvider());
    }

    @Test
    void verifySsoPolicySerialization() {
        val policyWritten = new DefaultRegisteredServiceAcceptableUsagePolicy();
        policyWritten.setEnabled(true);
        policyWritten.setMessageCode("example.code");
        policyWritten.setText("example text");

        val registeredService = new CasRegisteredService();
        registeredService.setAcceptableUsagePolicy(policyWritten);

        val policy = new ChainingRegisteredServiceSingleSignOnParticipationPolicy();
        policy.addPolicies(Arrays.asList(
            new LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy(TimeUnit.SECONDS, 100, 1),
            new AuthenticationDateRegisteredServiceSingleSignOnParticipationPolicy(TimeUnit.SECONDS, 100, 1)));
        registeredService.setSingleSignOnParticipationPolicy(policy);
        val results = serializer.toString(registeredService);
        val read = serializer.from(results);
        assertEquals(registeredService, read);
    }

    @Test
    void verifyEmptyStringAsNull() {
        val json = """
              {
                  "@class" : "org.apereo.cas.services.CasRegisteredService",
                      "serviceId" : "^https://xyz.*",
                      "name" : "XYZ",
                      "id" : "20161214"
            "authenticationPolicy" : {
              "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAuthenticationPolicy"}}""".indent(2);

        val serialized = serializer.from(json);
        assertNotNull(serialized);
        assertNotNull(serialized.getAuthenticationPolicy());
        assertNull(serialized.getAuthenticationPolicy().getCriteria());
    }
}
