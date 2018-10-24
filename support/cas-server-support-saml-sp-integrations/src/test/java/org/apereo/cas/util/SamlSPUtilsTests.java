package org.apereo.cas.util;

import org.apereo.cas.category.FileSystemCategory;
import org.apereo.cas.configuration.model.support.saml.sps.SamlServiceProviderProperties;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;

import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlSPUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Category(FileSystemCategory.class)
@TestPropertySource(properties = {"cas.authn.samlIdp.metadata.location=file:/tmp"})
public class SamlSPUtilsTests extends BaseSamlIdPConfigurationTests {
    @BeforeAll
    public static void beforeClass() {
        METADATA_DIRECTORY = new FileSystemResource(FileUtils.getTempDirectoryPath());
    }

    @AfterAll
    public static void shutdown() {
        val cols = FileUtils.listFiles(METADATA_DIRECTORY.getFile(), new String[]{"crt", "key", "xml"}, false);
        cols.forEach(FileUtils::deleteQuietly);
    }

    @Test
    public void verifyNewSamlServiceProvider() throws Exception {
        val entity = mock(EntityDescriptor.class);
        when(entity.getEntityID()).thenReturn("https://dropbox.com");
        val resolver = mock(SamlRegisteredServiceCachingMetadataResolver.class);
        val metadata = mock(MetadataResolver.class);
        when(metadata.resolveSingle(any(CriteriaSet.class))).thenReturn(entity);
        when(resolver.resolve(any(SamlRegisteredService.class))).thenReturn(metadata);
        val sp = new SamlServiceProviderProperties.Dropbox();
        sp.setMetadata("https://metadata.dropbox.com");
        sp.setEntityIds(CollectionUtils.wrap(entity.getEntityID()));
        val service = SamlSPUtils.newSamlServiceProviderService(sp, resolver);
        assertNotNull(service);
        assertEquals(entity.getEntityID(), service.getServiceId());
    }
}
