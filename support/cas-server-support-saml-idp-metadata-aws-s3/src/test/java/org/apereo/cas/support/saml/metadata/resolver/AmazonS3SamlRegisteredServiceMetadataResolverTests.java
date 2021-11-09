package org.apereo.cas.support.saml.metadata.resolver;

import org.apereo.cas.config.AmazonS3SamlIdPMetadataConfiguration;
import org.apereo.cas.config.AmazonS3SamlMetadataConfiguration;
import org.apereo.cas.config.SamlIdPAmazonS3RegisteredServiceMetadataConfiguration;
import org.apereo.cas.support.saml.BaseSamlIdPMetadataTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AmazonS3SamlRegisteredServiceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    AmazonS3SamlMetadataConfiguration.class,
    AmazonS3SamlIdPMetadataConfiguration.class,
    SamlIdPAmazonS3RegisteredServiceMetadataConfiguration.class,
    BaseSamlIdPMetadataTests.SharedTestConfiguration.class
}, properties = {
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/saml",
    
    "cas.authn.saml-idp.metadata.amazon-s3.bucket-name=cassamlmetadata",
    "cas.authn.saml-idp.metadata.amazon-s3.endpoint=http://127.0.0.1:4566",
    "cas.authn.saml-idp.metadata.amazon-s3.credential-access-key=test",
    "cas.authn.saml-idp.metadata.amazon-s3.credential-secret-key=test",
    "cas.authn.saml-idp.metadata.amazon-s3.crypto.enabled=false"
})
@EnabledIfPortOpen(port = 4566)
@Tag("AmazonWebServices")
public class AmazonS3SamlRegisteredServiceMetadataResolverTests {
    @Autowired
    @Qualifier("amazonS3SamlRegisteredServiceMetadataResolver")
    private SamlRegisteredServiceMetadataResolver amazonS3SamlRegisteredServiceMetadataResolver;

    @Test
    public void verifyAction() throws Exception {
        val service = new SamlRegisteredService();
        service.setName("SAML");
        service.setId(100);
        service.setMetadataLocation("awss3://");
        assertTrue(amazonS3SamlRegisteredServiceMetadataResolver.resolve(service).isEmpty());
        assertFalse(amazonS3SamlRegisteredServiceMetadataResolver.supports(null));
        assertTrue(amazonS3SamlRegisteredServiceMetadataResolver.supports(service));
        assertTrue(amazonS3SamlRegisteredServiceMetadataResolver.isAvailable(service));

        val signature =
            "MIICNTCCAZ6gAwIBAgIES343gjANBgkqhkiG9w0BAQUFADBVMQswCQYDVQQGEwJVUzELMAkGA1UE"
            + "CAwCQ0ExFjAUBgNVBAcMDU1vdW50YWluIFZpZXcxDTALBgNVBAoMBFdTTzIxEjAQBgNVBAMMCWxv"
            + "Y2FsaG9zdDAeFw0xMDAyMTkwNzAyMjZaFw0zNTAyMTMwNzAyMjZaMFUxCzAJBgNVBAYTAlVTMQsw"
            + "CQYDVQQIDAJDQTEWMBQGA1UEBwwNTW91bnRhaW4gVmlldzENMAsGA1UECgwEV1NPMjESMBAGA1UE"
            + "AwwJbG9jYWxob3N0MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCUp/oV1vWc8/TkQSiAvTou"
            + "sMzOM4asB2iltr2QKozni5aVFu818MpOLZIr8LMnTzWllJvvaA5RAAdpbECb+48FjbBe0hseUdN5"
            + "HpwvnH/DW8ZccGvk53I6Orq7hLCv1ZHtuOCokghz/ATrhyPq+QktMfXnRS4HrKGJTzxaCcU7OQID"
            + "AQABoxIwEDAOBgNVHQ8BAf8EBAMCBPAwDQYJKoZIhvcNAQEFBQADgYEAW5wPR7cr1LAdq+IrR44i"
            + "QlRG5ITCZXY9hI0PygLP2rHANh+PYfTmxbuOnykNGyhM6FjFLbW2uZHQTY1jMrPprjOrmyK5sjJR"
            + "O4d1DeGHT/YnIjs9JogRKv4XHECwLtIVdAbIdWHEtVZJyMSktcyysFcvuhPQK8Qc/E/Wq8uHSCo=";

        val doc = SamlMetadataDocument.builder()
            .id(RandomUtils.nextInt())
            .name("SAMLDocument")
            .signature(signature)
            .value(IOUtils.toString(new ClassPathResource("sp-metadata.xml").getInputStream(), StandardCharsets.UTF_8))
            .build();

        amazonS3SamlRegisteredServiceMetadataResolver.saveOrUpdate(doc);
        assertFalse(amazonS3SamlRegisteredServiceMetadataResolver.resolve(service).isEmpty());
    }
}
