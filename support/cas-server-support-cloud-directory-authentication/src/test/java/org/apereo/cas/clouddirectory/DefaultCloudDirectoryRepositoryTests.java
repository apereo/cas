package org.apereo.cas.clouddirectory;

import com.amazonaws.services.clouddirectory.AmazonCloudDirectory;
import com.amazonaws.services.clouddirectory.model.AttributeKey;
import com.amazonaws.services.clouddirectory.model.AttributeKeyAndValue;
import com.amazonaws.services.clouddirectory.model.IndexAttachment;
import com.amazonaws.services.clouddirectory.model.ListIndexResult;
import com.amazonaws.services.clouddirectory.model.ListObjectAttributesResult;
import com.amazonaws.services.clouddirectory.model.TypedAttributeValue;
import org.apereo.cas.configuration.model.support.clouddirectory.CloudDirectoryProperties;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultCloudDirectoryRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RefreshAutoConfiguration.class)
public class DefaultCloudDirectoryRepositoryTests {
    @Test
    public void verifyAction() {
        final var cloud = mock(AmazonCloudDirectory.class);
        final var result = new ListIndexResult();
        when(cloud.listIndex(any())).thenReturn(result);
        final var r = new DefaultCloudDirectoryRepository(cloud, new CloudDirectoryProperties());
        assertTrue(r.getUser("casuser").isEmpty());

    }

    @Test
    public void verifyActionIndexResult() {
        final var cloud = mock(AmazonCloudDirectory.class);
        final var result = new ListIndexResult();
        final var attach = new IndexAttachment();

        final var attr1 = new AttributeKeyAndValue();
        final var key1 = new AttributeKey();
        final var value1 = new TypedAttributeValue();
        key1.setName("AttrName1");
        value1.setStringValue("AttrValue");
        attr1.setKey(key1);
        attr1.setValue(value1);

        final var attr2 = new AttributeKeyAndValue();
        final var key2 = new AttributeKey();
        final var value2 = new TypedAttributeValue();
        key2.setName("AttrName2");
        value2.setNumberValue("123456");
        attr2.setKey(key2);
        attr2.setValue(value2);

        final var attr3 = new AttributeKeyAndValue();
        final var key3 = new AttributeKey();
        final var value3 = new TypedAttributeValue();
        key3.setName("AttrName3");
        value3.setBooleanValue(Boolean.TRUE);
        attr3.setKey(key3);
        attr3.setValue(value3);


        final var attr4 = new AttributeKeyAndValue();
        final var key4 = new AttributeKey();
        final var value4 = new TypedAttributeValue();
        key4.setName("AttrName4");
        value4.setDatetimeValue(new Date());
        attr4.setKey(key4);
        attr4.setValue(value4);

        final var attr5 = new AttributeKeyAndValue();
        final var key5 = new AttributeKey();
        final var value5 = new TypedAttributeValue();
        key5.setName("AttrName5");
        value5.setBinaryValue(ByteBuffer.wrap("Testing".getBytes(StandardCharsets.UTF_8)));
        attr5.setKey(key5);
        attr5.setValue(value5);

        attach.setIndexedAttributes(CollectionUtils.wrapList(attr1, attr2, attr3, attr4, attr5));

        result.setIndexAttachments(CollectionUtils.wrap(attach));
        when(cloud.listIndex(any())).thenReturn(result);

        final var attrResult = new ListObjectAttributesResult();
        attrResult.setAttributes(CollectionUtils.wrapList(attr1, attr2, attr3, attr4, attr5));

        when(cloud.listObjectAttributes(any())).thenReturn(attrResult);
        final var r = new DefaultCloudDirectoryRepository(cloud, new CloudDirectoryProperties());
        assertFalse(r.getUserInfoFromIndexResult(result).isEmpty());
    }
}
