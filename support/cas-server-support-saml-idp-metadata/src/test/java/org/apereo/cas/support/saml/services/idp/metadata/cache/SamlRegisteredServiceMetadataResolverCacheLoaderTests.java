package org.apereo.cas.support.saml.services.idp.metadata.cache;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.BaseSamlIdPServicesTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.ClasspathResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.FileSystemResourceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.DefaultSamlRegisteredServiceMetadataResolutionPlan;
import org.apereo.cas.util.RandomUtils;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import static org.apereo.cas.util.junit.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlRegisteredServiceMetadataResolverCacheLoaderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("SAMLMetadata")
class SamlRegisteredServiceMetadataResolverCacheLoaderTests extends BaseSamlIdPServicesTests {
    @Test
    void verifyClasspathByExpression() throws Throwable {
        System.setProperty("CLASSPATH_SP", "classpath:sample-sp.xml");
        val loader = buildCacheLoader(new ClasspathResourceMetadataResolver(new SamlIdPProperties(), openSamlConfigBean));
        val service = new SamlRegisteredService();
        service.setName(RandomUtils.randomAlphabetic(4));
        service.setId(RandomUtils.nextLong());
        service.setServiceId("https://example.org/saml");
        service.setMetadataLocation("${#systemProperties['CLASSPATH_SP']}");
        val key = new SamlRegisteredServiceCacheKey(service, new CriteriaSet());
        assertNotNull(loader.load(key));
    }

    @Test
    void verifyFileByExpression() throws Throwable {
        val mdFile = File.createTempFile("spsamlmetadata", ".xml");
        val content = IOUtils.toString(new ClassPathResource("sample-sp.xml").getInputStream(), StandardCharsets.UTF_8);
        FileUtils.writeStringToFile(mdFile, content, StandardCharsets.UTF_8);
        System.setProperty("FILE_EXPR_SP", mdFile.getCanonicalPath());

        val loader = buildCacheLoader();
        val service = new SamlRegisteredService();
        service.setName(RandomUtils.randomAlphabetic(4));
        service.setId(RandomUtils.nextLong());
        service.setServiceId("https://example.org/saml");
        service.setMetadataLocation("${#systemProperties['FILE_EXPR_SP']}");
        val key = new SamlRegisteredServiceCacheKey(service, new CriteriaSet());
        assertNotNull(loader.load(key));
    }

    @Test
    void verifyEmptyResolvers() throws Throwable {
        val plan = new DefaultSamlRegisteredServiceMetadataResolutionPlan();
        val loader = new SamlRegisteredServiceMetadataResolverCacheLoader(openSamlConfigBean, httpClient, plan);
        val service = new SamlRegisteredService();
        service.setName(RandomUtils.randomAlphabetic(4));
        service.setId(RandomUtils.nextLong());
        service.setServiceId("https://example.org/saml");
        service.setMetadataLocation("${#systemProperties['EMPTY_SP_REF']}");
        val key = new SamlRegisteredServiceCacheKey(service, new CriteriaSet());
        assertThrowsWithRootCause(RuntimeException.class, SamlException.class, () -> loader.load(key));
    }

    private SamlRegisteredServiceMetadataResolverCacheLoader buildCacheLoader() throws Throwable {
        val file = new File(FileUtils.getTempDirectory(), RandomUtils.randomAlphabetic(4));
        if (!file.mkdirs()) {
            fail(() -> "Failed to create directory " + file);
        }
        val props = new SamlIdPProperties();
        props.getMetadata().getFileSystem().setLocation(file.getCanonicalPath());
        return buildCacheLoader(new FileSystemResourceMetadataResolver(props, openSamlConfigBean));
    }

    private SamlRegisteredServiceMetadataResolverCacheLoader buildCacheLoader(final SamlRegisteredServiceMetadataResolver resolver) {
        val plan = new DefaultSamlRegisteredServiceMetadataResolutionPlan();
        plan.registerMetadataResolver(resolver);
        return new SamlRegisteredServiceMetadataResolverCacheLoader(openSamlConfigBean, httpClient, plan);
    }
}
