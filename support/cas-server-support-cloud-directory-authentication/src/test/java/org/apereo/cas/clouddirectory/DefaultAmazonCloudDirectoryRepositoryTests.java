package org.apereo.cas.clouddirectory;

import org.apereo.cas.configuration.model.support.clouddirectory.AmazonCloudDirectoryProperties;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.clouddirectory.CloudDirectoryClient;
import software.amazon.awssdk.services.clouddirectory.model.AttributeKey;
import software.amazon.awssdk.services.clouddirectory.model.AttributeKeyAndValue;
import software.amazon.awssdk.services.clouddirectory.model.IndexAttachment;
import software.amazon.awssdk.services.clouddirectory.model.ListIndexRequest;
import software.amazon.awssdk.services.clouddirectory.model.ListIndexResponse;
import software.amazon.awssdk.services.clouddirectory.model.ListObjectAttributesRequest;
import software.amazon.awssdk.services.clouddirectory.model.ListObjectAttributesResponse;
import software.amazon.awssdk.services.clouddirectory.model.ObjectReference;
import software.amazon.awssdk.services.clouddirectory.model.TypedAttributeValue;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultAmazonCloudDirectoryRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("AmazonWebServices")
public class DefaultAmazonCloudDirectoryRepositoryTests {
    private static DefaultAmazonCloudDirectoryRepository getMockCloudDirectoryRepository(final CloudDirectoryClient cloud,
                                                                                         final ListObjectAttributesRequest request) {
        return new DefaultAmazonCloudDirectoryRepository(cloud, new AmazonCloudDirectoryProperties()) {
            @Override
            protected ListIndexRequest getListIndexRequest(final String username, final ObjectReference reference) {
                return ListIndexRequest.builder().build();
            }

            @Override
            protected ObjectReference getObjectReference() {
                return ObjectReference.builder().build();
            }

            @Override
            protected ListObjectAttributesRequest getListObjectAttributesRequest(final String identifier) {
                return request;
            }
        };
    }

    @Test
    public void verifyAction() {
        val cloud = mock(CloudDirectoryClient.class);
        val result = ListIndexResponse.builder().build();
        when(cloud.listIndex(any(ListIndexRequest.class))).thenReturn(result);
        val r = new DefaultAmazonCloudDirectoryRepository(cloud, new AmazonCloudDirectoryProperties());
        assertTrue(r.getUser("casuser").isEmpty());
        assertNotNull(r.getListIndexRequest("casuser", ObjectReference.builder().build()));
    }

    @Test
    public void verifyNoAttachment() {
        val cloud = mock(CloudDirectoryClient.class);
        val result = ListIndexResponse.builder().indexAttachments(List.of()).build();
        when(cloud.listIndex(any(ListIndexRequest.class))).thenReturn(result);
        val r = getMockCloudDirectoryRepository(cloud, null);
        assertNull(r.getUser("casuser"));
    }

    @Test
    public void verifyNoAttributeRequest() {
        val cloud = mock(CloudDirectoryClient.class);
        val attachment = IndexAttachment.builder().objectIdentifier(UUID.randomUUID().toString()).build();
        val result = ListIndexResponse.builder().indexAttachments(List.of(attachment)).build();
        when(cloud.listIndex(any(ListIndexRequest.class))).thenReturn(result);
        val r = getMockCloudDirectoryRepository(cloud, null);
        assertNull(r.getUser("casuser"));
    }

    @Test
    public void verifyNoAttributeResult() {
        val cloud = mock(CloudDirectoryClient.class);
        val attachment = IndexAttachment.builder().objectIdentifier(UUID.randomUUID().toString()).build();
        val result = ListIndexResponse.builder().indexAttachments(List.of(attachment)).build();
        when(cloud.listIndex(any(ListIndexRequest.class))).thenReturn(result);

        when(cloud.listObjectAttributes(any(ListObjectAttributesRequest.class))).thenReturn(ListObjectAttributesResponse.builder().build());
        val r = getMockCloudDirectoryRepository(cloud, ListObjectAttributesRequest.builder().build());
        assertNull(r.getUser("casuser"));
    }

    @Test
    @SuppressWarnings("JdkObsolete")
    public void verifyActionIndexResult() {
        val cloud = mock(CloudDirectoryClient.class);

        val attr1 = AttributeKeyAndValue.builder()
            .key(AttributeKey.builder().name("AttrName1").build())
            .value(TypedAttributeValue.builder().stringValue("AttrValue").build())
            .build();

        val attr2 = AttributeKeyAndValue.builder()
            .key(AttributeKey.builder().name("AttrName2").build())
            .value(TypedAttributeValue.builder().numberValue("123456").build())
            .build();

        val attr3 = AttributeKeyAndValue.builder()
            .key(AttributeKey.builder().name("AttrName3").build())
            .value(TypedAttributeValue.builder().booleanValue(Boolean.TRUE).build())
            .build();

        val attr4 = AttributeKeyAndValue.builder()
            .key(AttributeKey.builder().name("AttrName4").build())
            .value(TypedAttributeValue.builder().datetimeValue(Instant.now(Clock.systemUTC())).build())
            .build();

        val attr5 = AttributeKeyAndValue.builder()
            .key(AttributeKey.builder().name("AttrName5").build())
            .value(TypedAttributeValue.builder()
                .binaryValue(SdkBytes.fromByteBuffer(ByteBuffer.wrap("Testing".getBytes(StandardCharsets.UTF_8)))).build())
            .build();

        val attach = IndexAttachment.builder()
            .indexedAttributes(CollectionUtils.wrapList(attr1, attr2, attr3, attr4, attr5))
            .build();

        val result = ListIndexResponse.builder().indexAttachments(List.of(attach)).build();
        when(cloud.listIndex(any(ListIndexRequest.class))).thenReturn(result);

        val attrResult = ListObjectAttributesResponse.builder().attributes(CollectionUtils.wrapList(attr1, attr2, attr3, attr4, attr5)).build();
        when(cloud.listObjectAttributes(any(ListObjectAttributesRequest.class))).thenReturn(attrResult);
        val r = new DefaultAmazonCloudDirectoryRepository(cloud, new AmazonCloudDirectoryProperties());
        assertFalse(r.getUserInfoFromIndexResult(result).isEmpty());
    }
}
