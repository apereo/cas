package org.apereo.cas.util.services;

import org.apereo.cas.services.AuthenticationDateRegisteredServiceSingleSignOnParticipationPolicy;
import org.apereo.cas.services.ChainingRegisteredServiceSingleSignOnParticipationPolicy;
import org.apereo.cas.services.LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultRegisteredServiceJsonSerializerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultRegisteredServiceJsonSerializerTests {

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
    public void verifySsoPolicySerialization() throws Exception {
        val s = new RegexRegisteredService();
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
