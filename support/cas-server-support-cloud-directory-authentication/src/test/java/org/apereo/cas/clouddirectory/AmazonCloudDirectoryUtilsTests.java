package org.apereo.cas.clouddirectory;

import org.apereo.cas.configuration.model.support.clouddirectory.AmazonCloudDirectoryProperties;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.clouddirectory.model.ObjectReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AmazonCloudDirectoryUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("AmazonWebServices")
public class AmazonCloudDirectoryUtilsTests {
    @Test
    public void verifyOperation() {
        assertNotNull(AmazonCloudDirectoryUtils.getListIndexRequest("attr-name", "attr-value",
            ObjectReference.builder().build(), new AmazonCloudDirectoryProperties()));
    }

}
