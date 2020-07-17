package org.apereo.cas.clouddirectory;

import org.apereo.cas.configuration.model.support.clouddirectory.CloudDirectoryProperties;
import org.apereo.cas.util.CollectionUtils;

import com.amazonaws.services.clouddirectory.AmazonCloudDirectory;
import com.amazonaws.services.clouddirectory.model.AttributeKey;
import com.amazonaws.services.clouddirectory.model.AttributeKeyAndValue;
import com.amazonaws.services.clouddirectory.model.IndexAttachment;
import com.amazonaws.services.clouddirectory.model.ListIndexRequest;
import com.amazonaws.services.clouddirectory.model.ListIndexResult;
import com.amazonaws.services.clouddirectory.model.ListObjectAttributesRequest;
import com.amazonaws.services.clouddirectory.model.ListObjectAttributesResult;
import com.amazonaws.services.clouddirectory.model.ObjectReference;
import com.amazonaws.services.clouddirectory.model.TypedAttributeValue;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultCloudDirectoryRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("AmazonWebServices")
public class DefaultCloudDirectoryRepositoryTests {
    @Test
    public void verifyAction() {
        val cloud = mock(AmazonCloudDirectory.class);
        val result = new ListIndexResult();
        when(cloud.listIndex(any())).thenReturn(result);
        val r = new DefaultCloudDirectoryRepository(cloud, new CloudDirectoryProperties());
        assertTrue(r.getUser("casuser").isEmpty());
    }

    @Test
    public void verifyNoAttachment() {
        val cloud = mock(AmazonCloudDirectory.class);
        val result = new ListIndexResult();
        result.setIndexAttachments(List.of());
        when(cloud.listIndex(any())).thenReturn(result);
        val r = getMockCloudDirectoryRepository(cloud, null);
        assertTrue(r.getUser("casuser").isEmpty());
    }

    @Test
    public void verifyNoAttributeRequest() {
        val cloud = mock(AmazonCloudDirectory.class);
        val result = new ListIndexResult();
        val attachment = new IndexAttachment();
        attachment.setObjectIdentifier(UUID.randomUUID().toString());
        result.setIndexAttachments(List.of(attachment));
        when(cloud.listIndex(any())).thenReturn(result);
        val r = getMockCloudDirectoryRepository(cloud, null);
        assertNull(r.getUser("casuser"));
    }

    @Test
    public void verifyNoAttributeResult() {
        val cloud = mock(AmazonCloudDirectory.class);
        val result = new ListIndexResult();
        val attachment = new IndexAttachment();
        attachment.setObjectIdentifier(UUID.randomUUID().toString());
        result.setIndexAttachments(List.of(attachment));
        when(cloud.listIndex(any())).thenReturn(result);
        when(cloud.listObjectAttributes(any())).thenReturn(new ListObjectAttributesResult());
        val r = getMockCloudDirectoryRepository(cloud, new ListObjectAttributesRequest());
        assertNull(r.getUser("casuser"));
    }

    @Test
    @SuppressWarnings("JdkObsolete")
    public void verifyActionIndexResult() {
        val cloud = mock(AmazonCloudDirectory.class);
        val result = new ListIndexResult();
        val attach = new IndexAttachment();

        val attr1 = new AttributeKeyAndValue();
        val key1 = new AttributeKey();
        val value1 = new TypedAttributeValue();
        key1.setName("AttrName1");
        value1.setStringValue("AttrValue");
        attr1.setKey(key1);
        attr1.setValue(value1);

        val attr2 = new AttributeKeyAndValue();
        val key2 = new AttributeKey();
        val value2 = new TypedAttributeValue();
        key2.setName("AttrName2");
        value2.setNumberValue("123456");
        attr2.setKey(key2);
        attr2.setValue(value2);

        val attr3 = new AttributeKeyAndValue();
        val key3 = new AttributeKey();
        val value3 = new TypedAttributeValue();
        key3.setName("AttrName3");
        value3.setBooleanValue(Boolean.TRUE);
        attr3.setKey(key3);
        attr3.setValue(value3);

        val attr4 = new AttributeKeyAndValue();
        val key4 = new AttributeKey();
        val value4 = new TypedAttributeValue();
        key4.setName("AttrName4");
        value4.setDatetimeValue(new Date());
        attr4.setKey(key4);
        attr4.setValue(value4);

        val attr5 = new AttributeKeyAndValue();
        val key5 = new AttributeKey();
        val value5 = new TypedAttributeValue();
        key5.setName("AttrName5");
        value5.setBinaryValue(ByteBuffer.wrap("Testing".getBytes(StandardCharsets.UTF_8)));
        attr5.setKey(key5);
        attr5.setValue(value5);

        attach.setIndexedAttributes(CollectionUtils.wrapList(attr1, attr2, attr3, attr4, attr5));

        result.setIndexAttachments(CollectionUtils.wrap(attach));
        when(cloud.listIndex(any())).thenReturn(result);

        val attrResult = new ListObjectAttributesResult();
        attrResult.setAttributes(CollectionUtils.wrapList(attr1, attr2, attr3, attr4, attr5));

        when(cloud.listObjectAttributes(any())).thenReturn(attrResult);
        val r = new DefaultCloudDirectoryRepository(cloud, new CloudDirectoryProperties());
        assertFalse(r.getUserInfoFromIndexResult(result).isEmpty());
    }

    private static DefaultCloudDirectoryRepository getMockCloudDirectoryRepository(final AmazonCloudDirectory cloud,
                                                                                   final ListObjectAttributesRequest request) {
        return new DefaultCloudDirectoryRepository(cloud, new CloudDirectoryProperties()) {
            @Override
            protected ListIndexRequest getListIndexRequest(final String username, final ObjectReference reference) {
                return mock(ListIndexRequest.class);
            }

            @Override
            protected ObjectReference getObjectReference() {
                return mock(ObjectReference.class);
            }

            @Override
            protected ListObjectAttributesRequest getListObjectAttributesRequest(final String identifier) {
                return request;
            }
        };
    }
}
