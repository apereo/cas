package org.apereo.cas.util;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.category.FileSystemCategory;
import org.apereo.cas.configuration.model.support.saml.sps.SamlServiceProviderProperties;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.util.Collection;

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
    @Autowired
    @Qualifier("defaultSamlRegisteredServiceCachingMetadataResolver")
    protected SamlRegisteredServiceCachingMetadataResolver defaultSamlRegisteredServiceCachingMetadataResolver;

    @BeforeClass
    public static void beforeClass() {
        METADATA_DIRECTORY = new FileSystemResource(FileUtils.getTempDirectoryPath());
    }

    @Test
    public void verifyNewSamlServiceProvider() throws Exception {
        final EntityDescriptor entity = mock(EntityDescriptor.class);
        when(entity.getEntityID()).thenReturn("https://dropbox.com");
        final SamlRegisteredServiceCachingMetadataResolver resolver = mock(SamlRegisteredServiceCachingMetadataResolver.class);
        final MetadataResolver metadata = mock(MetadataResolver.class);
        when(metadata.resolveSingle(any(CriteriaSet.class))).thenReturn(entity);
        when(resolver.resolve(any(SamlRegisteredService.class))).thenReturn(metadata);
        final SamlServiceProviderProperties.Dropbox sp = new SamlServiceProviderProperties.Dropbox();
        sp.setMetadata("https://metadata.dropbox.com");
        sp.setEntityIds(CollectionUtils.wrap(entity.getEntityID()));
        final SamlRegisteredService service = SamlSPUtils.newSamlServiceProviderService(sp, resolver);
        assertNotNull(service);
        assertEquals(entity.getEntityID(), service.getServiceId());
    }

    @Test
    public void verifySaveOperation() {
        final SamlServiceProviderProperties.TestShib sp = new SamlServiceProviderProperties.TestShib();
        sp.setMetadata("http://www.testshib.org/metadata/testshib-providers.xml");
        final SamlRegisteredService service = SamlSPUtils.newSamlServiceProviderService(sp, defaultSamlRegisteredServiceCachingMetadataResolver);
        SamlSPUtils.saveService(service, servicesManager);
        SamlSPUtils.saveService(service, servicesManager);
        assertEquals(2, servicesManager.count());
    }

    @AfterClass
    public static void shutdown() {
        final Collection<File> cols = FileUtils.listFiles(METADATA_DIRECTORY.getFile(), new String[]{"crt", "key", "xml"}, false);
        cols.forEach(FileUtils::deleteQuietly);
    }
}
