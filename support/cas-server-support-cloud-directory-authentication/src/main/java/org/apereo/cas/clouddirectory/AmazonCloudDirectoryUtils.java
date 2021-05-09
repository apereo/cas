package org.apereo.cas.clouddirectory;


import org.apereo.cas.configuration.model.support.clouddirectory.AmazonCloudDirectoryProperties;

import lombok.experimental.UtilityClass;
import lombok.val;
import software.amazon.awssdk.services.clouddirectory.model.AttributeKey;
import software.amazon.awssdk.services.clouddirectory.model.ListIndexRequest;
import software.amazon.awssdk.services.clouddirectory.model.ListObjectAttributesRequest;
import software.amazon.awssdk.services.clouddirectory.model.ObjectAttributeRange;
import software.amazon.awssdk.services.clouddirectory.model.ObjectReference;
import software.amazon.awssdk.services.clouddirectory.model.RangeMode;
import software.amazon.awssdk.services.clouddirectory.model.TypedAttributeValue;
import software.amazon.awssdk.services.clouddirectory.model.TypedAttributeValueRange;

/**
 * This is {@link AmazonCloudDirectoryUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@UtilityClass
public class AmazonCloudDirectoryUtils {

    /**
     * Gets object ref by id.
     *
     * @param objectId the object id
     * @return the object ref by id
     */
    public static ObjectReference getObjectRefById(final String objectId) {
        return getObjectRefByPath('$' + objectId);
    }


    /**
     * Gets list object attributes request.
     *
     * @param arnName  the arn name
     * @param objectId the object id
     * @return the list object attributes request
     */
    public static ListObjectAttributesRequest getListObjectAttributesRequest(final String arnName,
                                                                             final String objectId) {
        return ListObjectAttributesRequest.builder()
            .directoryArn(arnName)
            .objectReference(getObjectRefById(objectId))
            .build();
    }

    /**
     * Gets object ref by path.
     *
     * @param path the path
     * @return the object ref by path
     */
    public static ObjectReference getObjectRefByPath(final String path) {
        if (path == null) {
            return null;
        }
        return ObjectReference.builder().selector(path).build();
    }

    /**
     * Gets list index request.
     *
     * @param attributeName  the attribute name
     * @param attributeValue the attribute value
     * @param reference      the reference
     * @param cloud          the cloud
     * @return the list index request
     */
    public static ListIndexRequest getListIndexRequest(final String attributeName,
                                                       final String attributeValue,
                                                       final ObjectReference reference,
                                                       final AmazonCloudDirectoryProperties cloud) {
        val range = getObjectAttributeRanges(cloud.getSchemaArn(), cloud.getFacetName(),
            attributeName, attributeValue);

        return ListIndexRequest.builder()
            .directoryArn(cloud.getDirectoryArn())
            .indexReference(reference)
            .rangesOnIndexedValues(range)
            .build();
    }

    /**
     * Gets object attribute ranges.
     *
     * @param schemaArn      the schema arn
     * @param facetName      the facet name
     * @param attributeName  the attribute name
     * @param attributeValue the attribute value
     * @return the object attribute ranges
     */
    public static ObjectAttributeRange getObjectAttributeRanges(final String schemaArn, final String facetName,
                                                                final String attributeName, final String attributeValue) {
        val attributeKey = getAttributeKey(schemaArn, facetName, attributeName);
        return ObjectAttributeRange.builder()
            .attributeKey(attributeKey)
            .range(TypedAttributeValueRange.builder()
                .startValue(getTypedAttributeValue(attributeValue))
                .endValue(getTypedAttributeValue(attributeValue))
                .startMode(RangeMode.INCLUSIVE.toString())
                .endMode(RangeMode.INCLUSIVE.toString())
                .build())
            .build();
    }

    /**
     * Gets attribute key.
     *
     * @param schemaArn     the schema arn
     * @param facetName     the facet name
     * @param attributeName the attribute name
     * @return the attribute key
     */
    private static AttributeKey getAttributeKey(final String schemaArn, final String facetName,
                                                final String attributeName) {
        return AttributeKey.builder()
            .facetName(facetName)
            .schemaArn(schemaArn)
            .name(attributeName)
            .build();
    }

    /**
     * Gets typed attribute value.
     *
     * @param attributeValue the attribute value
     * @return the typed attribute value
     */
    private static TypedAttributeValue getTypedAttributeValue(final String attributeValue) {
        return TypedAttributeValue.builder().stringValue(attributeValue).build();
    }
}
