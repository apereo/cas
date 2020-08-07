package org.apereo.cas.support.saml.metadata.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import net.shibboleth.utilities.java.support.xml.BasicParserPool;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AmazonS3SamlRegisteredServiceMetadataResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("AmazonWebServices")
public class AmazonS3SamlRegisteredServiceMetadataResolverTests {
    @Test
    public void verifyAction() throws Exception {
        val client = mock(S3Client.class);

        val object = S3Object.builder().key("SAML-Document.xml").size(1000L).build();
        val result = ListObjectsV2Response.builder().keyCount(1)
            .contents(object).build();
        when(client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(result);

        val response = GetObjectResponse.builder().metadata(CollectionUtils.wrap("signature",
            "MIICNTCCAZ6gAwIBAgIES343gjANBgkqhkiG9w0BAQUFADBVMQswCQYDVQQGEwJVUzELMAkGA1UE"
                + "CAwCQ0ExFjAUBgNVBAcMDU1vdW50YWluIFZpZXcxDTALBgNVBAoMBFdTTzIxEjAQBgNVBAMMCWxv"
                + "Y2FsaG9zdDAeFw0xMDAyMTkwNzAyMjZaFw0zNTAyMTMwNzAyMjZaMFUxCzAJBgNVBAYTAlVTMQsw"
                + "CQYDVQQIDAJDQTEWMBQGA1UEBwwNTW91bnRhaW4gVmlldzENMAsGA1UECgwEV1NPMjESMBAGA1UE"
                + "AwwJbG9jYWxob3N0MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCUp/oV1vWc8/TkQSiAvTou"
                + "sMzOM4asB2iltr2QKozni5aVFu818MpOLZIr8LMnTzWllJvvaA5RAAdpbECb+48FjbBe0hseUdN5"
                + "HpwvnH/DW8ZccGvk53I6Orq7hLCv1ZHtuOCokghz/ATrhyPq+QktMfXnRS4HrKGJTzxaCcU7OQID"
                + "AQABoxIwEDAOBgNVHQ8BAf8EBAMCBPAwDQYJKoZIhvcNAQEFBQADgYEAW5wPR7cr1LAdq+IrR44i"
                + "QlRG5ITCZXY9hI0PygLP2rHANh+PYfTmxbuOnykNGyhM6FjFLbW2uZHQTY1jMrPprjOrmyK5sjJR"
                + "O4d1DeGHT/YnIjs9JogRKv4XHECwLtIVdAbIdWHEtVZJyMSktcyysFcvuhPQK8Qc/E/Wq8uHSCo="))
            .build();
        val is = new ResponseInputStream<>(response, AbortableInputStream.create(new ClassPathResource("sp-metadata.xml").getInputStream()));
        when(client.getObject(any(GetObjectRequest.class))).thenReturn(is);

        val properties = new SamlIdPProperties();
        properties.getMetadata().getAmazonS3().setBucketName("CAS");

        val parserPool = new BasicParserPool();
        parserPool.initialize();
        val configBean = new OpenSamlConfigBean(parserPool);
        assertNotNull(configBean.getUnmarshallerFactory());
        assertNotNull(configBean.getBuilderFactory());
        assertNotNull(configBean.getMarshallerFactory());
        assertNotNull(configBean.getParserPool());

        val r = new AmazonS3SamlRegisteredServiceMetadataResolver(
            properties, configBean, client);

        val service = new SamlRegisteredService();
        service.setName("SAML");
        service.setId(100);
        assertFalse(r.resolve(service).isEmpty());
    }
}
