package org.apereo.cas.util;

import module java.base;
import org.apereo.cas.configuration.model.support.saml.sps.SamlServiceProviderProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.CachedMetadataResolverResult;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlSPUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("SAMLServiceProvider")
@TestPropertySource(properties = "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata-test")
class SamlSPUtilsTests extends BaseSamlIdPConfigurationTests {

    @AfterAll
    public static void shutdown() {
        val file = new File(FileUtils.getTempDirectoryPath(), "idp-metadata-test");
        val cols = FileUtils.listFiles(file, new String[]{"crt", "key", "xml"}, false);
        cols.forEach(FileUtils::deleteQuietly);
    }

    @Test
    void verifyNewSamlServiceProvider() throws Throwable {
        val entity = mock(EntityDescriptor.class);
        when(entity.getEntityID()).thenReturn(RegisteredServiceTestUtils.CONST_TEST_URL);
        val resolver = mock(SamlRegisteredServiceCachingMetadataResolver.class);

        val metadataResolver = mock(MetadataResolver.class);
        when(metadataResolver.resolveSingle(any(CriteriaSet.class))).thenReturn(entity);

        val result = CachedMetadataResolverResult.builder().metadataResolver(metadataResolver).build();
        when(resolver.resolve(any(SamlRegisteredService.class), any(CriteriaSet.class))).thenReturn(result);

        val sp = new SamlServiceProviderProperties.Dropbox();
        sp.setMetadata("https://metadata.dropbox.com");
        sp.setEntityIds(CollectionUtils.wrap(RegisteredServiceTestUtils.CONST_TEST_URL));
        val service = SamlSPUtils.newSamlServiceProviderService(sp, resolver);
        assertNotNull(service);
        assertEquals(RegisteredServiceTestUtils.CONST_TEST_URL, service.getServiceId());
    }
}
