package org.apereo.cas.services.util;

import org.apereo.cas.services.AuthenticationDateRegisteredServiceSingleSignOnParticipationPolicy;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.ChainingRegisteredServiceSingleSignOnParticipationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceAcceptableUsagePolicy;
import org.apereo.cas.services.LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy;

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
public class RegisteredServiceJsonSerializerTests {

    @Test
    public void verifyPrinter() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val zer = new RegisteredServiceJsonSerializer(applicationContext);
        assertFalse(zer.supports(new File("bad-file")));
        assertFalse(zer.getContentTypes().isEmpty());
    }

    @Test
    public void verifyWriter() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val zer = new RegisteredServiceJsonSerializer(applicationContext);
        val writer = new StringWriter();
        zer.to(writer, new CasRegisteredService());
        assertNotNull(zer.from(writer));
    }

    @Test
    public void checkNullability() {
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

        val s = (CasRegisteredService) zer.from(json);
        assertNotNull(s);
        assertNotNull(s.getAccessStrategy());
        assertNotNull(s.getAttributeReleasePolicy());
        assertNotNull(s.getProxyPolicy());
        assertNotNull(s.getUsernameAttributeProvider());
    }

    @Test
    public void verifySsoPolicySerialization() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val policyWritten = new DefaultRegisteredServiceAcceptableUsagePolicy();
        policyWritten.setEnabled(true);
        policyWritten.setMessageCode("example.code");
        policyWritten.setText("example text");

        val s = new CasRegisteredService();
        s.setAcceptableUsagePolicy(policyWritten);

        val policy = new ChainingRegisteredServiceSingleSignOnParticipationPolicy();
        policy.addPolicies(Arrays.asList(
            new LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy(TimeUnit.SECONDS, 100, 1),
            new AuthenticationDateRegisteredServiceSingleSignOnParticipationPolicy(TimeUnit.SECONDS, 100, 1)));
        s.setSingleSignOnParticipationPolicy(policy);
        val zer = new RegisteredServiceJsonSerializer(applicationContext);
        val results = zer.toString(s);
        val read = zer.from(results);
        assertEquals(s, read);
    }

    @Test
    public void verifyEmptyStringAsNull() {
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
