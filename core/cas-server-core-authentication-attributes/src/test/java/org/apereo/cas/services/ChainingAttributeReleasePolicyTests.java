package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.ChainingPrincipalAttributesRepository;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.configuration.model.core.authentication.PrincipalAttributesCoreProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ScriptResourceCacheManager;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ChainingAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("AttributeRelease")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class
})
class ChainingAttributeReleasePolicyTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("scriptResourceCacheManager")
    private ScriptResourceCacheManager<String, ExecutableCompiledScript> scriptResourceCacheManager;

    private ChainingAttributeReleasePolicy chain;

    @BeforeEach
    void initialize() {
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext,
            scriptResourceCacheManager, ScriptResourceCacheManager.BEAN_NAME);
        configureChainingReleasePolicy(0, 0);
    }

    @Test
    void verifyOperationWithReplaceAndOrder() {
        configureChainingReleasePolicy(10, 1);
        val service = CoreAuthenticationTestUtils.getService(UUID.randomUUID().toString());
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService(service.getId());

        chain.setMergingPolicy(PrincipalAttributesCoreProperties.MergingStrategyTypes.REPLACE);
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(service)
            .principal(CoreAuthenticationTestUtils.getPrincipal())
            .applicationContext(applicationContext)
            .build();
        val results = chain.getAttributes(releasePolicyContext);
        assertTrue(results.containsKey("givenName"));
        val values = CollectionUtils.toCollection(results.get("givenName"));
        assertEquals(1, values.size());
        assertEquals("CasUserPolicy1", values.iterator().next().toString());
    }

    @Test
    void verifyOperationWithReplace() {
        chain.setMergingPolicy(PrincipalAttributesCoreProperties.MergingStrategyTypes.REPLACE);
        val service = CoreAuthenticationTestUtils.getService(UUID.randomUUID().toString());
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService(service.getId());
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(service)
            .principal(CoreAuthenticationTestUtils.getPrincipal())
            .applicationContext(applicationContext)
            .build();
        val results = chain.getAttributes(releasePolicyContext);
        assertTrue(results.containsKey("givenName"));
        val values = CollectionUtils.toCollection(results.get("givenName"));
        assertEquals(1, values.size());
        assertEquals("CasUserPolicy2", values.iterator().next().toString());

        val repository = chain.getPrincipalAttributesRepository();
        assertInstanceOf(ChainingPrincipalAttributesRepository.class, repository);
        assertNotNull(repository.getAttributes(releasePolicyContext));
        assertDoesNotThrow(() -> repository.update(releasePolicyContext.getPrincipal().getId(),
            releasePolicyContext.getPrincipal().getAttributes(), releasePolicyContext));
    }

    @Test
    void verifyOperationWithAdd() {
        val service = CoreAuthenticationTestUtils.getService(UUID.randomUUID().toString());
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService(service.getId());
        chain.setMergingPolicy(PrincipalAttributesCoreProperties.MergingStrategyTypes.ADD);
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(service)
            .principal(CoreAuthenticationTestUtils.getPrincipal())
            .applicationContext(applicationContext)
            .build();
        val results = chain.getAttributes(releasePolicyContext);
        assertTrue(results.containsKey("givenName"));
        val values = CollectionUtils.toCollection(results.get("givenName"));
        assertEquals(1, values.size());
        assertEquals("CasUserPolicy1", values.iterator().next().toString());
    }

    @Test
    void verifyOperationWithMultivalued() {
        val service = CoreAuthenticationTestUtils.getService(UUID.randomUUID().toString());
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService(service.getId());
        chain.setMergingPolicy(PrincipalAttributesCoreProperties.MergingStrategyTypes.MULTIVALUED);
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(service)
            .principal(CoreAuthenticationTestUtils.getPrincipal())
            .applicationContext(applicationContext)
            .build();
        val results = chain.getAttributes(releasePolicyContext);
        assertTrue(results.containsKey("givenName"));
        val values = CollectionUtils.toCollection(results.get("givenName"));
        assertEquals(2, values.size());
        assertTrue(values.contains("CasUserPolicy1"));
        assertTrue(values.contains("CasUserPolicy2"));
    }

    @Test
    void verifyConsentableAttrs() {
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAuthenticationTestUtils.getPrincipal())
            .applicationContext(applicationContext)
            .build();
        chain.setMergingPolicy(PrincipalAttributesCoreProperties.MergingStrategyTypes.MULTIVALUED);
        val results = chain.getConsentableAttributes(context);
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
