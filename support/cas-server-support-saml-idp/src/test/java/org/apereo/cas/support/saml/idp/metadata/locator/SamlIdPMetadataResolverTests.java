package org.apereo.cas.support.saml.idp.metadata.locator;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;

import com.google.common.collect.Iterables;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.springframework.core.io.FileSystemResource;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.TestPropertySource;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlIdPMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
@TestPropertySource(properties = "cas.authn.saml-idp.entityId=https://cas.example.org/idp")
@EnableRetry
public class SamlIdPMetadataResolverTests extends BaseSamlIdPConfigurationTests {
    @BeforeAll
    public static void beforeThisClass() throws Exception {
        METADATA_DIRECTORY = new FileSystemResource(new File(FileUtils.getTempDirectory(), "idp-metadata"));
        if (METADATA_DIRECTORY.exists()) {
            FileUtils.deleteDirectory(METADATA_DIRECTORY.getFile());
        }
    }

    @Test
    public void verifyOperation() throws Exception {
        val criteria = new CriteriaSet(new EntityIdCriterion(casProperties.getAuthn().getSamlIdp().getEntityId()));

        val result1 = casSamlIdPMetadataResolver.resolve(criteria);
        assertFalse(Iterables.isEmpty(result1));

        val result2 = casSamlIdPMetadataResolver.resolve(criteria);
        assertFalse(Iterables.isEmpty(result2));

        assertEquals(Iterables.size(result1), Iterables.size(result2));
    }

    @Test
    public void verifyOperationEmpty() throws Exception {
        val criteria = new CriteriaSet(new EntityIdCriterion("https://example.com"));
        val result = casSamlIdPMetadataResolver.resolve(criteria);
        assertTrue(Iterables.isEmpty(result));
    }

    @Test
    public void verifyOperationFail() {
        val criteria = mock(CriteriaSet.class);
        when(criteria.get(any())).thenThrow(IllegalArgumentException.class);
        assertThrows(ResolverException.class, () -> casSamlIdPMetadataResolver.resolve(criteria));
    }
}
