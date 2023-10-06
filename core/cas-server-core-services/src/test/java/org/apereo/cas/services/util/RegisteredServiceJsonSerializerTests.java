package org.apereo.cas.services.util;

import org.apereo.cas.services.AuthenticationDateRegisteredServiceSingleSignOnParticipationPolicy;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.ChainingRegisteredServiceSingleSignOnParticipationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceAcceptableUsagePolicy;
import org.apereo.cas.services.LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
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
class RegisteredServiceJsonSerializerTests {

    @Test
    void verifyPrinter() throws Throwable {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val zer = new RegisteredServiceJsonSerializer(applicationContext);
        assertFalse(zer.supports(new File("bad-file")));
        assertFalse(zer.getContentTypes().isEmpty());
    }

    @Test
    void verifyWriter() throws Throwable {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val zer = new RegisteredServiceJsonSerializer(applicationContext);
        val writer = new StringWriter();
        zer.to(writer, new CasRegisteredService());
        assertNotNull(zer.from(writer));
    }

    @Test
    void verifyList() throws Throwable {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val zer = new RegisteredServiceJsonSerializer(applicationContext);
        val registeredService = new CasRegisteredService();
        registeredService.setName("CasService");
        val results = zer.fromList(CollectionUtils.wrapList(registeredService));
        assertNotNull(zer.fromList(results));
    }

    @Test
    void checkNullability() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val zer = new RegisteredServiceJsonSerializer(applicationContext);
        val json = """
            {
                "@class" : "org.apereo.cas.services.CasRegisteredService",
                    "serviceId" : "^https://xyz.*",
                    "name" : "XYZ",
                    "id" : "20161214"
            }""".indent(4);

        val registeredService = (CasRegisteredService) zer.from(json);
        assertNotNull(registeredService);
        assertNotNull(registeredService.getAccessStrategy());
        assertNotNull(registeredService.getAttributeReleasePolicy());
        assertNotNull(registeredService.getProxyPolicy());
        assertNotNull(registeredService.getUsernameAttributeProvider());
    }

    @Test
    void verifySsoPolicySerialization() throws Throwable {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
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
        val zer = new RegisteredServiceJsonSerializer(applicationContext);
        val results = zer.toString(registeredService);
        val read = zer.from(results);
        assertEquals(registeredService, read);
    }

    @Test
    void verifyEmptyStringAsNull() throws Throwable {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val zer = new RegisteredServiceJsonSerializer(applicationContext);
        val json = """
              {
                  "@class" : "org.apereo.cas.services.CasRegisteredService",
                      "serviceId" : "^https://xyz.*",
                      "name" : "XYZ",
                      "id" : "20161214"
            "authenticationPolicy" : {
              "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAuthenticationPolicy"}}""".indent(2);

        val serialized = zer.from(json);
        assertNotNull(serialized);
        assertNotNull(serialized.getAuthenticationPolicy());
        assertNull(serialized.getAuthenticationPolicy().getCriteria());
    }
}
