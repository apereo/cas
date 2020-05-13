package org.apereo.cas.services.util;

import org.apereo.cas.services.AuthenticationDateRegisteredServiceSingleSignOnParticipationPolicy;
import org.apereo.cas.services.ChainingRegisteredServiceSingleSignOnParticipationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceAcceptableUsagePolicy;
import org.apereo.cas.services.LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy;
import org.apereo.cas.services.RegexRegisteredService;

import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
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
        val zer = new RegisteredServiceJsonSerializer(new MinimalPrettyPrinter());
        assertFalse(zer.supports(new File("bad-file")));
    }

    @Test
    public void checkNullability() {
        val zer = new RegisteredServiceJsonSerializer();
        val json = "    {\n"
            + "        \"@class\" : \"org.apereo.cas.services.RegexRegisteredService\",\n"
            + "            \"serviceId\" : \"^https://xyz.*\",\n"
            + "            \"name\" : \"XYZ\",\n"
            + "            \"id\" : \"20161214\"\n"
            + "    }";

        val s = zer.from(json);
        assertNotNull(s);
        assertNotNull(s.getAccessStrategy());
        assertNotNull(s.getAttributeReleasePolicy());
        assertNotNull(s.getProxyPolicy());
        assertNotNull(s.getUsernameAttributeProvider());
    }

    @Test
    public void verifySsoPolicySerialization() {
        val policyWritten = new DefaultRegisteredServiceAcceptableUsagePolicy();
        policyWritten.setEnabled(true);
        policyWritten.setMessageCode("example.code");
        policyWritten.setText("example text");

        val s = new RegexRegisteredService();
        s.setAcceptableUsagePolicy(policyWritten);

        val policy = new ChainingRegisteredServiceSingleSignOnParticipationPolicy();
        policy.addPolicies(Arrays.asList(
            new LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy(TimeUnit.SECONDS, 100, 1),
            new AuthenticationDateRegisteredServiceSingleSignOnParticipationPolicy(TimeUnit.SECONDS, 100, 1)));
        s.setSingleSignOnParticipationPolicy(policy);
        val zer = new RegisteredServiceJsonSerializer();
        val results = zer.toString(s);
        val read = zer.from(results);
        assertEquals(s, read);
    }
}
