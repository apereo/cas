package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ChainingRegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("RegisteredService")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
class ChainingRegisteredServiceAccessStrategyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyDelegatedAccessAnd() {
        val chain = new ChainingRegisteredServiceAccessStrategy();
        chain.setOperator(LogicalOperatorTypes.AND);
        var s1 = new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("key1", Set.of("value1")));
        s1.setDelegatedAuthenticationPolicy(new DefaultRegisteredServiceDelegatedAuthenticationPolicy()
            .setExclusive(true).setPermitUndefined(true).setAllowedProviders(List.of("P1")));
        chain.addStrategy(s1);

        var s2 = new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("key2", Set.of("value2")));
        s2.setDelegatedAuthenticationPolicy(new DefaultRegisteredServiceDelegatedAuthenticationPolicy()
            .setExclusive(false).setPermitUndefined(false).setAllowedProviders(List.of("P2")));
        chain.addStrategy(s2);

        val delegation = chain.getDelegatedAuthenticationPolicy();
        assertNotNull(delegation);
        assertFalse(delegation.isExclusive());
        assertFalse(delegation.isPermitUndefined());
        assertTrue(delegation.getAllowedProviders().contains("P1"));
        assertTrue(delegation.getAllowedProviders().contains("P2"));
        assertFalse(delegation.isProviderRequired());
        assertFalse(delegation.isProviderAllowed("P2", new CasRegisteredService()));
    }

    @Test
    void verifyDelegatedAccessOr() {
        val chain = new ChainingRegisteredServiceAccessStrategy();
        chain.setOperator(LogicalOperatorTypes.OR);
        var s1 = new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("key1", Set.of("value1")));
        s1.setDelegatedAuthenticationPolicy(new DefaultRegisteredServiceDelegatedAuthenticationPolicy()
            .setExclusive(true).setPermitUndefined(true).setAllowedProviders(List.of("P1")));
        chain.addStrategy(s1);

        var s2 = new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("key2", Set.of("value2")));
        s2.setDelegatedAuthenticationPolicy(new DefaultRegisteredServiceDelegatedAuthenticationPolicy()
            .setExclusive(false).setPermitUndefined(false).setAllowedProviders(List.of("P2")));
        chain.addStrategy(s2);

        val delegation = chain.getDelegatedAuthenticationPolicy();
        assertNotNull(delegation);
        assertTrue(delegation.isExclusive());
        assertTrue(delegation.isPermitUndefined());
        assertTrue(delegation.isProviderRequired());

        assertTrue(delegation.isProviderAllowed("P1", new CasRegisteredService()));
    }

    @Test
    void verifyRequiredAttributes() {
        val chain = new ChainingRegisteredServiceAccessStrategy();
        chain.setOperator(LogicalOperatorTypes.AND);
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("key1", Set.of("value1"))));
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("key2", Set.of("value2"))));
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("key1", Set.of("value3"))));
        val attributes = chain.getRequiredAttributes();
        assertEquals(2, attributes.size());
        assertTrue(attributes.containsKey("key1"));
        assertTrue(attributes.containsKey("key2"));
        assertTrue(attributes.get("key1").contains("value1"));
        assertTrue(attributes.get("key1").contains("value3"));
        assertTrue(attributes.get("key2").contains("value2"));
    }

    @Test
    void verifyAndOperation() {
        val chain = new ChainingRegisteredServiceAccessStrategy();
        chain.setOperator(LogicalOperatorTypes.AND);
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(false, true));
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(true, false));
        assertFalse(chain.isServiceAccessAllowed(CoreAuthenticationTestUtils.getRegisteredService(), CoreAuthenticationTestUtils.getService()));
        assertFalse(chain.isServiceAccessAllowedForSso(CoreAuthenticationTestUtils.getRegisteredService()));
    }

    @Test
    void verifyPrincipalAccessAnd() {
        val chain = new ChainingRegisteredServiceAccessStrategy();
        chain.setOperator(LogicalOperatorTypes.AND);
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("key1", Set.of("value1"))));
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("key2", Set.of("value2"))));

        assertFalse(chain.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().principalId("casuser")
            .applicationContext(applicationContext)
            .attributes(CollectionUtils.wrap("key1", Set.of("value1"))).build()));
        assertFalse(chain.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().principalId("casuser")
            .applicationContext(applicationContext)
            .attributes(CollectionUtils.wrap("key2", Set.of("value2"))).build()));
        assertTrue(chain.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().principalId("casuser")
            .applicationContext(applicationContext)
            .attributes(CollectionUtils.wrap("key1", Set.of("value1"), "key2", Set.of("value2"))).build()));
    }

    @Test
    void verifyPrincipalAccessOr() {
        val chain = new ChainingRegisteredServiceAccessStrategy();
        chain.setOperator(LogicalOperatorTypes.OR);
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("key1", Set.of("value1"))));
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("key2", Set.of("value2"))));

        assertTrue(chain.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().principalId("casuser")
            .applicationContext(applicationContext)
            .attributes(CollectionUtils.wrap("key1", Set.of("value1"))).build()));
        assertTrue(chain.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().principalId("casuser")
            .applicationContext(applicationContext)
            .attributes(CollectionUtils.wrap("key2", Set.of("value2"))).build()));
    }

    @Test
    void verifyOrOperation() throws Throwable {
        val chain = new ChainingRegisteredServiceAccessStrategy();
        chain.setUnauthorizedRedirectUrl(new URI("https://google.com"));
        chain.setOperator(LogicalOperatorTypes.OR);
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(false, true));
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(true, false));
        assertTrue(chain.isServiceAccessAllowed(CoreAuthenticationTestUtils.getRegisteredService(), CoreAuthenticationTestUtils.getService()));
        assertTrue(chain.isServiceAccessAllowedForSso(CoreAuthenticationTestUtils.getRegisteredService()));
        assertNotNull(chain.getUnauthorizedRedirectUrl());
    }


    @Test
    void verifySerializeToJson() throws Throwable {
        val chain = new ChainingRegisteredServiceAccessStrategy();
        chain.setUnauthorizedRedirectUrl(new URI("https://google.com"));
        chain.setOperator(LogicalOperatorTypes.OR);
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(false, true));
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(true, false));

        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        MAPPER.writeValue(jsonFile, chain);
        val policyRead = MAPPER.readValue(jsonFile, ChainingRegisteredServiceAccessStrategy.class);
        assertEquals(chain, policyRead);
    }

    @Test
    void verifyPrincipalAccessMixedRules() {
        val chain1 = new ChainingRegisteredServiceAccessStrategy();
        chain1.setOperator(LogicalOperatorTypes.AND);
        chain1.addStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("key1", Set.of("value1"))));
        chain1.addStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("key2", Set.of("value2"))));

        val chain2 = new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("key3", Set.of("value3")));

        val parentChain = new ChainingRegisteredServiceAccessStrategy();
        parentChain.setOperator(LogicalOperatorTypes.OR);
        parentChain.addStrategies(chain1, chain2);

        assertFalse(parentChain.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().principalId("casuser")
            .applicationContext(applicationContext)
            .attributes(CollectionUtils.wrap("key1", Set.of("value1"))).build()));
        assertFalse(parentChain.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().principalId("casuser")
            .applicationContext(applicationContext)
            .attributes(CollectionUtils.wrap("key2", Set.of("value2"))).build()));

        assertTrue(parentChain.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().principalId("casuser")
            .applicationContext(applicationContext)
            .attributes(CollectionUtils.wrap("key1", Set.of("value1"), "key2", Set.of("value2"))).build()));
        assertTrue(parentChain.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder().principalId("casuser")
            .applicationContext(applicationContext)
            .attributes(CollectionUtils.wrap("key3", Set.of("value3"))).build()));
    }
}
