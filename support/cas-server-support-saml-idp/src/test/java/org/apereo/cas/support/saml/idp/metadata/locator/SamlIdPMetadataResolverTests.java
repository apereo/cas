package org.apereo.cas.support.saml.idp.metadata.locator;

import module java.base;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.idp.metadata.SamlIdPMetadataResolver;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import com.google.common.collect.Iterables;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.metadata.criteria.entity.impl.EvaluableEntityRoleEntityDescriptorCriterion;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlIdPMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAMLMetadata")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata99"
})
class SamlIdPMetadataResolverTests extends BaseSamlIdPConfigurationTests {

    @RepeatedTest(2)
    void verifyOperation() throws Throwable {
        val criteria = new CriteriaSet(new EntityIdCriterion(casProperties.getAuthn().getSamlIdp().getCore().getEntityId()));
        val result1 = casSamlIdPMetadataResolver.resolve(criteria);
        assertFalse(Iterables.isEmpty(result1));
        val result2 = casSamlIdPMetadataResolver.resolve(criteria);
        assertFalse(Iterables.isEmpty(result2));
        assertEquals(Iterables.size(result1), Iterables.size(result2));
    }

    @RepeatedTest(2)
    void verifyOperationWithoutEntityId() throws Throwable {
        val criteria = new CriteriaSet(new EvaluableEntityRoleEntityDescriptorCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME));
        val result1 = casSamlIdPMetadataResolver.resolve(criteria);
        assertFalse(Iterables.isEmpty(result1));
        assertEquals(casProperties.getAuthn().getSamlIdp().getCore().getEntityId(),
            Iterables.getFirst(result1, null).getEntityID());
    }

    @Test
    void verifyOperationWithService() throws Throwable {
        val criteria = new CriteriaSet(
            new SamlIdPSamlRegisteredServiceCriterion(getSamlRegisteredServiceFor(UUID.randomUUID().toString())),
            new EntityIdCriterion(casProperties.getAuthn().getSamlIdp().getCore().getEntityId()));

        var locator = mock(SamlIdPMetadataLocator.class);
        when(locator.shouldGenerateMetadataFor(any())).thenReturn(true);
        when(locator.exists(any())).thenReturn(false);
        when(locator.resolveMetadata(any())).thenReturn(new ByteArrayResource(ArrayUtils.EMPTY_BYTE_ARRAY));
        val resolver = new SamlIdPMetadataResolver(locator, mock(SamlIdPMetadataGenerator.class), openSamlConfigBean, casProperties);
        val result1 = resolver.resolve(criteria);
        assertTrue(Iterables.isEmpty(result1));
    }

    @Test
    void verifyConcurrentPerServiceResolutionDoesNotLeakMetadata() throws Throwable {
        val serviceCount = 64;
        val globalEntityId = casProperties.getAuthn().getSamlIdp().getCore().getEntityId();
        val globalMetadata = samlIdPMetadataLocator.resolveMetadata(Optional.empty())
            .getContentAsString(StandardCharsets.UTF_8);

        val services = new ArrayList<SamlRegisteredService>(serviceCount);
        val entityIds = new ArrayList<String>(serviceCount);
        val metadataByEntityId = new HashMap<Long, String>(serviceCount);
        for (var i = 0; i < serviceCount; i++) {
            val entityId = "https://idp.example.org/concurrent/" + i;
            val service = getSamlRegisteredServiceFor(entityId);
            service.setId(i);
            service.setName("ConcurrentService-" + i);
            services.add(service);
            entityIds.add(entityId);
            metadataByEntityId.put(service.getId(), globalMetadata.replace(globalEntityId, entityId));
        }

        val locator = mock(SamlIdPMetadataLocator.class);
        when(locator.exists(any())).thenReturn(true);
        when(locator.shouldGenerateMetadataFor(any())).thenReturn(false);
        when(locator.resolveMetadata(any())).thenAnswer(invocation -> {
            val given = (Optional<SamlRegisteredService>) invocation.getArgument(0);
            val metadata = given.map(service -> metadataByEntityId.get(service.getId())).orElse(globalMetadata);
            return new ByteArrayResource(metadata.getBytes(StandardCharsets.UTF_8));
        });

        val resolver = new SamlIdPMetadataResolver(locator, mock(SamlIdPMetadataGenerator.class),
            openSamlConfigBean, casProperties);
        resolver.setId(UUID.randomUUID().toString());
        resolver.setRequireValidMetadata(false);
        resolver.setFailFastInitialization(false);
        resolver.initialize();

        val startGate = new CountDownLatch(1);
        val failures = new ConcurrentLinkedQueue<String>();
        val futures = new ArrayList<Future<?>>(serviceCount);
        try (val executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (var i = 0; i < serviceCount; i++) {
                val index = i;
                futures.add(executor.submit(() -> {
                    startGate.await();
                    val expectedEntityId = entityIds.get(index);
                    val criteria = new CriteriaSet(
                        new SamlIdPSamlRegisteredServiceCriterion(services.get(index)),
                        new EntityIdCriterion(expectedEntityId));
                    val resolved = Iterables.getFirst(resolver.resolve(criteria), null);
                    if (resolved == null) {
                        failures.add("No metadata resolved for " + expectedEntityId);
                    } else if (!expectedEntityId.equals(resolved.getEntityID())) {
                        failures.add("Expected " + expectedEntityId + " but resolved " + resolved.getEntityID());
                    }
                    return null;
                }));
            }
            startGate.countDown();
            for (val future : futures) {
                future.get();
            }
        }
        assertTrue(failures.isEmpty(), () -> "Concurrent resolution crossed service boundaries: " + String.join("; ", failures));
    }

    @RepeatedTest(2)
    void verifyOperationEmpty() throws Throwable {
        val criteria = new CriteriaSet(new EntityIdCriterion("https://example.com"));
        val result = casSamlIdPMetadataResolver.resolve(criteria);
        assertTrue(Iterables.isEmpty(result));
    }
}
