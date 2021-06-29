package org.apereo.cas.support.saml.services.idp.metadata.cache;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.BaseSamlIdPServicesTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.ClasspathResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.FileSystemResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.DefaultSamlRegisteredServiceMetadataResolutionPlan;

import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlRegisteredServiceMetadataResolverCacheLoaderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("SAMLMetadata")
public class SamlRegisteredServiceMetadataResolverCacheLoaderTests extends BaseSamlIdPServicesTests {
    @Test
    public void verifyClasspathByExpression() throws Exception {
        System.setProperty("SP_REF", "classpath:sample-sp.xml");
        val props = new SamlIdPProperties();
        props.getMetadata().getFileSystem().setLocation(new FileSystemResource(FileUtils.getTempDirectory()).getFile().getCanonicalPath());

        val plan = new DefaultSamlRegisteredServiceMetadataResolutionPlan();
        plan.registerMetadataResolver(new ClasspathResourceMetadataResolver(props, openSamlConfigBean));
        val loader = new SamlRegisteredServiceMetadataResolverCacheLoader(openSamlConfigBean, httpClient, plan);

        val service = new SamlRegisteredService();
        service.setName("Example");
        service.setId(1000);
        service.setServiceId("https://example.org/saml");
        service.setMetadataLocation("${#systemProperties['SP_REF']}");
        val key = new SamlRegisteredServiceCacheKey(service, new CriteriaSet());
        assertNotNull(loader.load(key));
    }

    @Test
    public void verifyFileByExpression() throws Exception {
        val mdFile = File.createTempFile("spsamlmetadata", ".xml");
        val content = IOUtils.toString(new ClassPathResource("sample-sp.xml").getInputStream(), StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(mdFile, content, StandardCharsets.UTF_8);
        System.setProperty("SP_REF", mdFile.getCanonicalPath());

        val props = new SamlIdPProperties();
        props.getMetadata().getFileSystem().setLocation(new FileSystemResource(FileUtils.getTempDirectory()).getFile().getCanonicalPath());

        val plan = new DefaultSamlRegisteredServiceMetadataResolutionPlan();
        plan.registerMetadataResolver(new FileSystemResourceMetadataResolver(props, openSamlConfigBean));
        val loader = new SamlRegisteredServiceMetadataResolverCacheLoader(openSamlConfigBean, httpClient, plan);

        val service = new SamlRegisteredService();
        service.setName("Example");
        service.setId(1000);
        service.setServiceId("https://example.org/saml");
        service.setMetadataLocation("${#systemProperties['SP_REF']}");
        val key = new SamlRegisteredServiceCacheKey(service, new CriteriaSet());
        assertNotNull(loader.load(key));
    }

    @Test
    public void verifyEmptyResolvers() {
        val plan = new DefaultSamlRegisteredServiceMetadataResolutionPlan();
        val loader = new SamlRegisteredServiceMetadataResolverCacheLoader(openSamlConfigBean, httpClient, plan);

        val service = new SamlRegisteredService();
        service.setName("Example");
        service.setId(1000);
        service.setServiceId("https://example.org/saml");
        service.setMetadataLocation("${#systemProperties['SP_REF']}");
        val key = new SamlRegisteredServiceCacheKey(service, new CriteriaSet());
        assertThrows(SamlException.class, () -> loader.load(key));
    }
}
