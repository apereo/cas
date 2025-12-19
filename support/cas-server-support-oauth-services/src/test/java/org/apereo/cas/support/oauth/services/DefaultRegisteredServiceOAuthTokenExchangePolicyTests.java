package org.apereo.cas.support.oauth.services;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultRegisteredServiceOAuthTokenExchangePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("OAuth")
class DefaultRegisteredServiceOAuthTokenExchangePolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "RegisteredServiceOAuthTokenExchangePolicyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyOperation() {
        val policy = new DefaultRegisteredServiceOAuthTokenExchangePolicy()
            .setAllowedAudience(CollectionUtils.wrapSet(".*"))
            .setAllowedTokenTypes(Set.of(".*access-token.*"))
            .setAllowedActorTokenTypes(Set.of(".*"));
        assertTrue(policy.isTokenExchangeAllowed(new OAuthRegisteredService(), Set.of("resource"),
            Set.of("audience"), "access-token"));
        MAPPER.writeValue(JSON_FILE, policy);
        val strategyRead = MAPPER.readValue(JSON_FILE, DefaultRegisteredServiceOAuthTokenExchangePolicy.class);
        assertEquals(policy, strategyRead);
    }

    @Test
    void verifyActorToken() {
        val policy = new DefaultRegisteredServiceOAuthTokenExchangePolicy()
            .setAllowedActorTokenTypes(Set.of(".*"))
            .setRequiredActorTokenAttributes(Map.of("common-name", List.of("nothing|.*cas")));
        val actor = CoreAuthenticationTestUtils.getAuthentication(
            CoreAuthenticationTestUtils.getPrincipal(UUID.randomUUID().toString(), Map.of("common-name", List.of("anything-cas"))));
        val subject = CoreAuthenticationTestUtils.getAuthentication(CoreAuthenticationTestUtils.getPrincipal(UUID.randomUUID().toString()));
        assertTrue(policy.canSubjectTokenActAs(subject, actor, "anything"));
    }
}
