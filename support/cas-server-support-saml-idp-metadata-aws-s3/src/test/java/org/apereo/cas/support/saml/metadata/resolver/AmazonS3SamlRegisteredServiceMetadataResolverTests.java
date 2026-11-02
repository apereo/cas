package org.apereo.cas.support.saml.metadata.resolver;

import module java.base;
import org.apereo.cas.config.CasAmazonS3SamlMetadataAutoConfiguration;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.BaseSamlIdPMetadataTests;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AmazonS3SamlRegisteredServiceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasAmazonS3SamlMetadataAutoConfiguration.class,
    BaseSamlIdPMetadataTests.SharedTestConfiguration.class
}, properties = {
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/saml22",

    "cas.authn.saml-idp.metadata.amazon-s3.bucket-name=cassamlmetadata",
    "cas.authn.saml-idp.metadata.amazon-s3.endpoint=http://127.0.0.1:4566",
    "cas.authn.saml-idp.metadata.amazon-s3.region=us-east-1",
    "cas.authn.saml-idp.metadata.amazon-s3.credential-access-key=test",
    "cas.authn.saml-idp.metadata.amazon-s3.credential-secret-key=test",
    "cas.authn.saml-idp.metadata.amazon-s3.crypto.enabled=false"
})
@EnabledIfListeningOnPort(port = 4566)
@Tag("AmazonWebServices")
@ExtendWith(CasTestExtension.class)
@ResourceLock("amazonS3SamlMetadataBucket")
class AmazonS3SamlRegisteredServiceMetadataResolverTests {
    @Autowired
    @Qualifier("amazonS3SamlRegisteredServiceMetadataResolver")
    private SamlRegisteredServiceMetadataResolver resolver;

    @Autowired
    @Qualifier(OpenSamlConfigBean.DEFAULT_BEAN_NAME)
    private OpenSamlConfigBean openSamlConfigBean;

    @BeforeEach
    void setup() {
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        metadataManager.removeAll();
    }

    @Test
    void verifyAction() throws Throwable {
        val service = new SamlRegisteredService();
        service.setName("SAML");
        service.setId(100);
        service.setMetadataLocation("awss3://");
        assertTrue(resolver.resolve(service).isEmpty());
        assertFalse(resolver.supports(null));
        assertTrue(resolver.supports(service));
        assertTrue(resolver.isAvailable(service));

        val signature =
            """
                MIICNTCCAZ6gAwIBAgIES343gjANBgkqhkiG9w0BAQUFADBVMQswCQYDVQQGEwJVUzELMAkGA1UE\
                CAwCQ0ExFjAUBgNVBAcMDU1vdW50YWluIFZpZXcxDTALBgNVBAoMBFdTTzIxEjAQBgNVBAMMCWxv\
                Y2FsaG9zdDAeFw0xMDAyMTkwNzAyMjZaFw0zNTAyMTMwNzAyMjZaMFUxCzAJBgNVBAYTAlVTMQsw\
                CQYDVQQIDAJDQTEWMBQGA1UEBwwNTW91bnRhaW4gVmlldzENMAsGA1UECgwEV1NPMjESMBAGA1UE\
                AwwJbG9jYWxob3N0MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCUp/oV1vWc8/TkQSiAvTou\
                sMzOM4asB2iltr2QKozni5aVFu818MpOLZIr8LMnTzWllJvvaA5RAAdpbECb+48FjbBe0hseUdN5\
                HpwvnH/DW8ZccGvk53I6Orq7hLCv1ZHtuOCokghz/ATrhyPq+QktMfXnRS4HrKGJTzxaCcU7OQID\
                AQABoxIwEDAOBgNVHQ8BAf8EBAMCBPAwDQYJKoZIhvcNAQEFBQADgYEAW5wPR7cr1LAdq+IrR44i\
                QlRG5ITCZXY9hI0PygLP2rHANh+PYfTmxbuOnykNGyhM6FjFLbW2uZHQTY1jMrPprjOrmyK5sjJR\
                O4d1DeGHT/YnIjs9JogRKv4XHECwLtIVdAbIdWHEtVZJyMSktcyysFcvuhPQK8Qc/E/Wq8uHSCo=""";
        val doc = buildDocument(signature);
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        metadataManager.store(doc);
        assertTrue(resolver.resolve(service).isEmpty());
        metadataManager.store(buildDocument());
        assertEquals(1, resolver.resolve(service).size());
    }

    @Test
    void verifyEntityIdCriterionSelectsMetadataDocument() throws Throwable {
        val entityId = "https://carmenwiki.osu.edu/shibboleth";
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        metadataManager.store(buildDocument());
        val otherDocument = buildDocument();
        otherDocument.setValue(otherDocument.getValue().replace(entityId, "https://other.example.org"));
        metadataManager.store(otherDocument);

        val service = new SamlRegisteredService();
        service.setName("SAML Service");
        service.setServiceId("^https://.+$");
        service.setMetadataLocation("awss3://");
        val resolvers = resolver.resolve(service, new CriteriaSet(new EntityIdCriterion(entityId)));
        assertEquals(1, resolvers.size());
    }

    @Test
    void verifyListingIsPagedAndOnlyMatchesAreDownloaded() throws Throwable {
        val entityId = "https://carmenwiki.osu.edu/shibboleth";
        val metadata = IOUtils.toString(new ClassPathResource("sp-metadata.xml").getInputStream(), StandardCharsets.UTF_8);
        val s3Client = mock(S3Client.class);
        when(s3Client.headBucket(any(HeadBucketRequest.class))).thenReturn(HeadBucketResponse.builder().build());
        when(s3Client.listObjectsV2Paginator(any(ListObjectsV2Request.class)))
            .thenAnswer(invocation -> new ListObjectsV2Iterable(s3Client, invocation.getArgument(0)));
        when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenAnswer(invocation -> {
            val request = (ListObjectsV2Request) invocation.getArgument(0);
            return request.continuationToken() == null
                ? ListObjectsV2Response.builder().isTruncated(true).nextContinuationToken("page-2")
                    .contents(S3Object.builder().key("other").eTag("etag-other").build()).build()
                : ListObjectsV2Response.builder().isTruncated(false)
                    .contents(S3Object.builder().key("carmenwiki").eTag("etag-carmenwiki").build()).build();
        });
        when(s3Client.headObject(any(HeadObjectRequest.class))).thenAnswer(invocation -> {
            val request = (HeadObjectRequest) invocation.getArgument(0);
            return HeadObjectResponse.builder()
                .metadata(Map.of("entityid", "carmenwiki".equals(request.key()) ? entityId : "https://other.example.org"))
                .build();
        });
        when(s3Client.getObject(any(GetObjectRequest.class))).thenAnswer(_ -> new ResponseInputStream<>(
            GetObjectResponse.builder().metadata(Map.of("entityid", entityId)).build(),
            AbortableInputStream.create(new ByteArrayInputStream(metadata.getBytes(StandardCharsets.UTF_8)))));

        val properties = new SamlIdPProperties();
        properties.getMetadata().getAmazonS3().setBucketName("paged-bucket");
        val metadataResolver = new AmazonS3SamlRegisteredServiceMetadataResolver(properties, openSamlConfigBean, s3Client);
        val service = new SamlRegisteredService();
        service.setName("SAML Service");
        service.setServiceId("^https://.+$");
        service.setMetadataLocation("awss3://");
        val criteria = new CriteriaSet(new EntityIdCriterion(entityId));
        assertEquals(1, metadataResolver.resolve(service, criteria).size());
        assertEquals(1, metadataResolver.resolve(service, criteria).size());

        verify(s3Client, times(4)).listObjectsV2(any(ListObjectsV2Request.class));
        verify(s3Client, times(2)).headObject(any(HeadObjectRequest.class));
        verify(s3Client, times(2)).getObject(argThat((GetObjectRequest request) -> "carmenwiki".equals(request.key())));
        verify(s3Client, never()).getObject(argThat((GetObjectRequest request) -> "other".equals(request.key())));
        verify(s3Client, never()).listBuckets();
    }

    @Test
    void verifyLoad() throws Throwable {
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        val doc = buildDocument();
        metadataManager.store(doc);

        val documents = metadataManager.load();
        assertFalse(documents.isEmpty());
        assertEquals(doc.getName(), documents.getFirst().getName());
    }

    @Test
    void verifyFindById() throws Throwable {
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        val doc = buildDocument();
        val storedDocument = metadataManager.store(doc);

        val found = metadataManager.findById(storedDocument.getId());
        assertTrue(found.isPresent());
        assertEquals(storedDocument.getName(), found.get().getName());
        assertTrue(metadataManager.findById(-999).isEmpty());
    }

    @Test
    void verifyFindByName() throws Throwable {
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        val doc = buildDocument();
        metadataManager.store(doc);

        val found = metadataManager.findByName(doc.getName());
        assertTrue(found.isPresent());
        assertEquals(doc.getName(), found.get().getName());
        assertTrue(metadataManager.findByName("Unknown").isEmpty());
    }

    @Test
    void verifyRemoveById() throws Throwable {
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        val doc = buildDocument();
        val storedDocument = metadataManager.store(doc);

        metadataManager.removeById(storedDocument.getId());
        assertTrue(metadataManager.findById(storedDocument.getId()).isEmpty());
    }

    @Test
    void verifyRemoveByName() throws Throwable {
        val metadataManager = resolver.getMetadataManager().orElseThrow();
        val doc = buildDocument();
        metadataManager.store(doc);
        metadataManager.removeByName(doc.getName());
        assertTrue(metadataManager.findByName(doc.getName()).isEmpty());
    }

    private static SamlMetadataDocument buildDocument() throws Exception {
        return SamlMetadataDocument.builder()
            .id(RandomUtils.nextInt())
            .name("SAMLDocument-%s".formatted(RandomUtils.nextInt()))
            .value(IOUtils.toString(new ClassPathResource("sp-metadata.xml").getInputStream(), StandardCharsets.UTF_8))
            .build();
    }

    private static SamlMetadataDocument buildDocument(final String signature) throws Exception {
        val document = buildDocument();
        document.setSignature(signature);
        return document;
    }
}
