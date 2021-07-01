package org.apereo.cas.support.saml.idp.metadata.locator;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;

import com.google.common.collect.Iterables;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.metadata.criteria.entity.impl.EvaluableEntityRoleEntityDescriptorCriterion;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

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
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata"
})
@EnableRetry
public class SamlIdPMetadataResolverTests extends BaseSamlIdPConfigurationTests {

    @RepeatedTest(2)
    public void verifyOperation() throws Exception {
        val criteria = new CriteriaSet(new EntityIdCriterion(casProperties.getAuthn().getSamlIdp().getCore().getEntityId()));
        val result1 = casSamlIdPMetadataResolver.resolve(criteria);
        assertFalse(Iterables.isEmpty(result1));
        val result2 = casSamlIdPMetadataResolver.resolve(criteria);
        assertFalse(Iterables.isEmpty(result2));
        assertEquals(Iterables.size(result1), Iterables.size(result2));
    }

    @RepeatedTest(2)
    public void verifyOperationWithoutEntityId() throws Exception {
        val criteria = new CriteriaSet(new EvaluableEntityRoleEntityDescriptorCriterion(IDPSSODescriptor.DEFAULT_ELEMENT_NAME));
        val result1 = casSamlIdPMetadataResolver.resolve(criteria);
        assertFalse(Iterables.isEmpty(result1));
        assertEquals(casProperties.getAuthn().getSamlIdp().getCore().getEntityId(),
            Iterables.getFirst(result1, null).getEntityID());
    }

    @Test
    public void verifyOperationWithService() throws Exception {
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

    @RepeatedTest(2)
    public void verifyOperationEmpty() throws Exception {
        val criteria = new CriteriaSet(new EntityIdCriterion("https://example.com"));
        val result = casSamlIdPMetadataResolver.resolve(criteria);
        assertTrue(Iterables.isEmpty(result));
    }
}
