package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ChainingRegisteredServiceDelegatedAuthenticationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Delegation")
class ChainingRegisteredServiceDelegatedAuthenticationPolicyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyChainReadFromJson() {
        val json = """
            {
              "@class": "org.apereo.cas.services.ChainingRegisteredServiceDelegatedAuthenticationPolicy",
              "operator": "OR",
              "strategies": [ "java.util.ArrayList", [
                {
                  "@class": "org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy",
                  "allowedProviders": [ "java.util.ArrayList", [ "GitHub" ] ],
                  "exclusive": true
                },
                {
                  "@class": "org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy",
                  "allowedProviders": [ "java.util.ArrayList", [ "Google" ] ]
                }
              ]]
            }
            """;
        val chain = (ChainingRegisteredServiceDelegatedAuthenticationPolicy) MAPPER.readValue(json, RegisteredServiceDelegatedAuthenticationPolicy.class);
        assertEquals(2, chain.getStrategies().size());
        assertEquals(LogicalOperatorTypes.OR, chain.getOperator());
        assertEquals(Set.of("GitHub", "Google"), Set.copyOf(chain.getAllowedProviders()));
        assertTrue(chain.isExclusive());
        val written = MAPPER.writeValueAsString(chain);
        assertEquals(chain, MAPPER.readValue(written, RegisteredServiceDelegatedAuthenticationPolicy.class));
    }
    @Test
    void verifySelectionStrategy() {
        val chain = new ChainingRegisteredServiceDelegatedAuthenticationPolicy();
        val p1 = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        val p2 = new DefaultRegisteredServiceDelegatedAuthenticationPolicy();
        p2.setSelectionStrategy("groovy { providers.first()}");
        chain.addStrategy(p1);
        chain.addStrategy(p2);
        assertTrue(chain.getSelectionStrategy().startsWith("groovy {"));
    }
}
