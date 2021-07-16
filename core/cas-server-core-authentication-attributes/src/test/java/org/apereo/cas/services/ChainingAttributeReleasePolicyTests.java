package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledGroovyScript;
import org.apereo.cas.util.scripting.ScriptResourceCacheManager;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ChainingAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Attributes")
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasCoreUtilConfiguration.class
})
public class ChainingAttributeReleasePolicyTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("scriptResourceCacheManager")
    private ScriptResourceCacheManager<String, ExecutableCompiledGroovyScript> scriptResourceCacheManager;


    private ChainingAttributeReleasePolicy chain;

    @BeforeEach
    public void initialize() {
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            scriptResourceCacheManager, ScriptResourceCacheManager.BEAN_NAME);
        configureChainingReleasePolicy(0, 0);
    }

    @Test
    public void verifyOperationWithReplaceAndOrder() {
        configureChainingReleasePolicy(10, 1);
        chain.setMergingPolicy(PrincipalAttributesCoreProperties.MergingStrategyTypes.REPLACE);
        val results = chain.getAttributes(CoreAuthenticationTestUtils.getPrincipal(),
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getRegisteredService());
        assertTrue(results.containsKey("givenName"));
        val values = CollectionUtils.toCollection(results.get("givenName"));
        assertEquals(1, values.size());
        assertEquals("CasUserPolicy1", values.iterator().next().toString());
    }

    @Test
    public void verifyOperationWithReplace() {
        chain.setMergingPolicy(PrincipalAttributesCoreProperties.MergingStrategyTypes.REPLACE);
        val results = chain.getAttributes(CoreAuthenticationTestUtils.getPrincipal(),
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getRegisteredService());
        assertTrue(results.containsKey("givenName"));
        val values = CollectionUtils.toCollection(results.get("givenName"));
        assertEquals(1, values.size());
        assertEquals("CasUserPolicy2", values.iterator().next().toString());
    }

    @Test
    public void verifyOperationWithAdd() {
        chain.setMergingPolicy(PrincipalAttributesCoreProperties.MergingStrategyTypes.ADD);
        val results = chain.getAttributes(CoreAuthenticationTestUtils.getPrincipal(),
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getRegisteredService());
        assertTrue(results.containsKey("givenName"));
        val values = CollectionUtils.toCollection(results.get("givenName"));
        assertEquals(1, values.size());
        assertEquals("CasUserPolicy1", values.iterator().next().toString());
    }

    @Test
    public void verifyOperationWithMultivalued() {
        chain.setMergingPolicy(PrincipalAttributesCoreProperties.MergingStrategyTypes.MULTIVALUED);
        val results = chain.getAttributes(CoreAuthenticationTestUtils.getPrincipal(),
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getRegisteredService());
        assertTrue(results.containsKey("givenName"));
        val values = CollectionUtils.toCollection(results.get("givenName"));
        assertEquals(2, values.size());
        assertTrue(values.contains("CasUserPolicy1"));
        assertTrue(values.contains("CasUserPolicy2"));
    }

    @Test
    public void verifyConsentableAttrs() {
        chain.setMergingPolicy(PrincipalAttributesCoreProperties.MergingStrategyTypes.MULTIVALUED);
        val results = chain.getConsentableAttributes(CoreAuthenticationTestUtils.getPrincipal(),
            CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getRegisteredService());
        assertTrue(results.containsKey("givenName"));
        val values = CollectionUtils.toCollection(results.get("givenName"));
        assertEquals(2, values.size());
    }

    private void configureChainingReleasePolicy(final int order1, final int order2) {
        chain = new ChainingAttributeReleasePolicy();

        val p1 = new ReturnMappedAttributeReleasePolicy();
        p1.setOrder(order1);
        p1.setAllowedAttributes(CollectionUtils.wrap("givenName", "groovy {return ['CasUserPolicy1']}"));

        val p2 = new ReturnMappedAttributeReleasePolicy();
        p2.setOrder(order2);
        p2.setAllowedAttributes(CollectionUtils.wrap("givenName", "groovy {return ['CasUserPolicy2']}"));

        chain.addPolicies(p1, p2);
    }
}
