package org.apereo.cas.services;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ChainingRegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("RegisteredService")
public class ChainingRegisteredServiceAccessStrategyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "ChainingRegisteredServiceAccessStrategyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    public void verifyDelegatedAccessAnd() {
        val chain = new ChainingRegisteredServiceAccessStrategy();
        chain.setOperator(RegisteredServiceChainOperatorTypes.AND);
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
        assertFalse(delegation.isProviderAllowed("P2", new RegexRegisteredService()));
    }

    @Test
    public void verifyDelegatedAccessOr() {
        val chain = new ChainingRegisteredServiceAccessStrategy();
        chain.setOperator(RegisteredServiceChainOperatorTypes.OR);
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

        assertTrue(delegation.isProviderAllowed("P1", new RegexRegisteredService()));
    }

    @Test
    public void verifyRequiredAttributes() {
        val chain = new ChainingRegisteredServiceAccessStrategy();
        chain.setOperator(RegisteredServiceChainOperatorTypes.AND);
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
    public void verifyAndOperation() {
        val chain = new ChainingRegisteredServiceAccessStrategy();
        chain.setOperator(RegisteredServiceChainOperatorTypes.AND);
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(false, true));
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(true, false));
        assertFalse(chain.isServiceAccessAllowed());
        assertFalse(chain.isServiceAccessAllowedForSso());
    }

    @Test
    public void verifyPrincipalAccessAnd() {
        val chain = new ChainingRegisteredServiceAccessStrategy();
        chain.setOperator(RegisteredServiceChainOperatorTypes.AND);
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("key1", Set.of("value1"))));
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("key2", Set.of("value2"))));

        assertFalse(chain.doPrincipalAttributesAllowServiceAccess("casuser", CollectionUtils.wrap("key1", Set.of("value1"))));
        assertFalse(chain.doPrincipalAttributesAllowServiceAccess("casuser", CollectionUtils.wrap("key2", Set.of("value2"))));
        assertTrue(chain.doPrincipalAttributesAllowServiceAccess("casuser",
            CollectionUtils.wrap("key1", Set.of("value1"), "key2", Set.of("value2"))));
    }

    @Test
    public void verifyPrincipalAccessOr() {
        val chain = new ChainingRegisteredServiceAccessStrategy();
        chain.setOperator(RegisteredServiceChainOperatorTypes.OR);
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("key1", Set.of("value1"))));
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("key2", Set.of("value2"))));

        assertTrue(chain.doPrincipalAttributesAllowServiceAccess("casuser", CollectionUtils.wrap("key1", Set.of("value1"))));
        assertTrue(chain.doPrincipalAttributesAllowServiceAccess("casuser", CollectionUtils.wrap("key2", Set.of("value2"))));
    }

    @Test
    public void verifySetAccess() {
        val chain = new ChainingRegisteredServiceAccessStrategy();
        chain.setOperator(RegisteredServiceChainOperatorTypes.AND);
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("key1", Set.of("value1"))));
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("key2", Set.of("value2"))));
        chain.setServiceAccessAllowed(true);
        assertTrue(chain.getStrategies().get(0).isServiceAccessAllowed());
        assertTrue(chain.getStrategies().get(1).isServiceAccessAllowed());
    }

    @Test
    public void verifyOrOperation() throws Exception {
        val chain = new ChainingRegisteredServiceAccessStrategy();
        chain.setUnauthorizedRedirectUrl(new URI("https://google.com"));
        chain.setOperator(RegisteredServiceChainOperatorTypes.OR);
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(false, true));
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(true, false));
        assertTrue(chain.isServiceAccessAllowed());
        assertTrue(chain.isServiceAccessAllowedForSso());
        assertNotNull(chain.getUnauthorizedRedirectUrl());
    }


    @Test
    public void verifySerializeToJson() throws Exception {
        val chain = new ChainingRegisteredServiceAccessStrategy();
        chain.setUnauthorizedRedirectUrl(new URI("https://google.com"));
        chain.setOperator(RegisteredServiceChainOperatorTypes.OR);
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(false, true));
        chain.addStrategy(new DefaultRegisteredServiceAccessStrategy(true, false));

        MAPPER.writeValue(JSON_FILE, chain);
        val policyRead = MAPPER.readValue(JSON_FILE, ChainingRegisteredServiceAccessStrategy.class);
        assertEquals(chain, policyRead);
    }

    @Test
    public void verifyPrincipalAccessMixedRules() throws Exception {
        val chain1 = new ChainingRegisteredServiceAccessStrategy();
        chain1.setOperator(RegisteredServiceChainOperatorTypes.AND);
        chain1.addStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("key1", Set.of("value1"))));
        chain1.addStrategy(new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("key2", Set.of("value2"))));

        val chain2 = new DefaultRegisteredServiceAccessStrategy(CollectionUtils.wrap("key3", Set.of("value3")));

        val parentChain = new ChainingRegisteredServiceAccessStrategy();
        parentChain.setOperator(RegisteredServiceChainOperatorTypes.OR);
        parentChain.addStrategies(chain1, chain2);

        assertFalse(parentChain.doPrincipalAttributesAllowServiceAccess("casuser",
            CollectionUtils.wrap("key1", Set.of("value1"))));
        assertFalse(parentChain.doPrincipalAttributesAllowServiceAccess("casuser",
            CollectionUtils.wrap("key2", Set.of("value2"))));

        assertTrue(parentChain.doPrincipalAttributesAllowServiceAccess("casuser",
            CollectionUtils.wrap("key1", Set.of("value1"), "key2", Set.of("value2"))));
        assertTrue(parentChain.doPrincipalAttributesAllowServiceAccess("casuser",
            CollectionUtils.wrap("key3", Set.of("value3"))));
    }
}
