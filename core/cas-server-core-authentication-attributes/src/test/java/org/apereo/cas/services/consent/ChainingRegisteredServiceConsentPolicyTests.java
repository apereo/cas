package org.apereo.cas.services.consent;

import module java.base;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.services.RegisteredServiceConsentPolicy;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ChainingRegisteredServiceConsentPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("RegisteredService")
class ChainingRegisteredServiceConsentPolicyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyChainReadFromJson() {
        val json = """
            {
              "@class": "org.apereo.cas.services.consent.ChainingRegisteredServiceConsentPolicy",
              "policies": [ "java.util.ArrayList", [
                {
                  "@class": "org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy",
                  "status": "TRUE",
                  "excludedAttributes": [ "java.util.LinkedHashSet", [ "cn" ] ]
                },
                {
                  "@class": "org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy",
                  "excludedAttributes": [ "java.util.LinkedHashSet", [ "mail" ] ]
                }
              ]]
            }
            """;
        val chain = (ChainingRegisteredServiceConsentPolicy) MAPPER.readValue(json, RegisteredServiceConsentPolicy.class);
        assertEquals(2, chain.getPolicies().size());
        assertEquals(Set.of("cn", "mail"), chain.getExcludedAttributes());
        assertTrue(chain.getStatus().isTrue());
        val written = MAPPER.writeValueAsString(chain);
        assertEquals(chain, MAPPER.readValue(written, RegisteredServiceConsentPolicy.class));
    }

    @Test
    void verifyOperation() {
        val chain = new ChainingRegisteredServiceConsentPolicy();
        chain.addPolicies(List.of(new DefaultRegisteredServiceConsentPolicy(Set.of("cn"), Set.of("givenName"))));
        chain.addPolicy(new DefaultRegisteredServiceConsentPolicy(Set.of("mail"), Set.of("address")));
        assertTrue(chain.getStatus().isUndefined());
        assertEquals(Set.of("cn", "mail"), chain.getExcludedAttributes());
        assertEquals(Set.of("givenName", "address"), chain.getIncludeOnlyAttributes());
    }

    @Test
    void verifyStatusEnabled() {
        val chain = new ChainingRegisteredServiceConsentPolicy();
        chain.addPolicies(List.of(
            new DefaultRegisteredServiceConsentPolicy().setStatus(TriStateBoolean.FALSE),
            new DefaultRegisteredServiceConsentPolicy().setStatus(TriStateBoolean.TRUE)));
        assertTrue(chain.getStatus().isTrue());
    }

    @Test
    void verifyStatusDisabled() {
        val chain = new ChainingRegisteredServiceConsentPolicy();
        chain.addPolicies(List.of(
            new DefaultRegisteredServiceConsentPolicy().setStatus(TriStateBoolean.FALSE),
            new DefaultRegisteredServiceConsentPolicy().setStatus(TriStateBoolean.FALSE)));
        assertTrue(chain.getStatus().isFalse());
    }

    @Test
    void verifyStatusUndefined() {
        val chain = new ChainingRegisteredServiceConsentPolicy();
        chain.addPolicies(List.of(
            new DefaultRegisteredServiceConsentPolicy(),
            new DefaultRegisteredServiceConsentPolicy()));
        assertTrue(chain.getStatus().isUndefined());
    }

    @Test
    void verifyExcludedServices() {
        val chain = new ChainingRegisteredServiceConsentPolicy();
        chain.addPolicies(List.of(
            new DefaultRegisteredServiceConsentPolicy().setStatus(TriStateBoolean.TRUE).setExcludedServices(Set.of("application1")),
            new DefaultRegisteredServiceConsentPolicy().setExcludedServices(Set.of("application2"))));
        assertTrue(chain.getStatus().isTrue());
        assertEquals(2, chain.getExcludedServices().size());
        assertTrue(chain.getExcludedServices().contains("application1"));
    }
}
