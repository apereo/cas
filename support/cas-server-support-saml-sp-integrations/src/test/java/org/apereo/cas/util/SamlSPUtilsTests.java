package org.apereo.cas.util;

import org.apereo.cas.configuration.model.support.saml.sps.SamlServiceProviderProperties;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;

import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.test.context.TestPropertySource;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlSPUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("SAML")
@TestPropertySource(properties = "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata-test")
public class SamlSPUtilsTests extends BaseSamlIdPConfigurationTests {

    @AfterAll
    public static void shutdown() {
        val file = new File(FileUtils.getTempDirectoryPath(), "idp-metadata-test");
        val cols = FileUtils.listFiles(file, new String[]{"crt", "key", "xml"}, false);
        cols.forEach(FileUtils::deleteQuietly);
    }

    @Test
    public void verifyNewSamlServiceProvider() throws Exception {
        val entity = mock(EntityDescriptor.class);
        when(entity.getEntityID()).thenReturn("https://dropbox.com");
        val resolver = mock(SamlRegisteredServiceCachingMetadataResolver.class);
        val metadata = mock(MetadataResolver.class);
        when(metadata.resolveSingle(any(CriteriaSet.class))).thenReturn(entity);
        when(resolver.resolve(any(SamlRegisteredService.class), any(CriteriaSet.class))).thenReturn(metadata);
        val sp = new SamlServiceProviderProperties.Dropbox();
        sp.setMetadata("https://metadata.dropbox.com");
        sp.setEntityIds(CollectionUtils.wrap(entity.getEntityID()));
        val service = SamlSPUtils.newSamlServiceProviderService(sp, resolver);
        assertNotNull(service);
        assertEquals(entity.getEntityID(), service.getServiceId());
    }
}
