package org.apereo.cas.clouddirectory;

import com.amazonaws.services.clouddirectory.model.AttributeKey;
import com.amazonaws.services.clouddirectory.model.AttributeKeyAndValue;
import com.amazonaws.services.clouddirectory.model.ListIndexRequest;
import com.amazonaws.services.clouddirectory.model.ListObjectAttributesRequest;
import com.amazonaws.services.clouddirectory.model.ListObjectAttributesResult;
import com.amazonaws.services.clouddirectory.model.ObjectAttributeRange;
import com.amazonaws.services.clouddirectory.model.ObjectReference;
import com.amazonaws.services.clouddirectory.model.RangeMode;
import com.amazonaws.services.clouddirectory.model.TypedAttributeValue;
import com.amazonaws.services.clouddirectory.model.TypedAttributeValueRange;
import org.apereo.cas.configuration.model.support.clouddirectory.CloudDirectoryProperties;

/**
 * This is {@link CloudDirectoryUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public final class CloudDirectoryUtils {
    
    private CloudDirectoryUtils() {
    }

    /**
     * Gets attribute key value by name.
     *
     * @param attributesResult the attributes result
     * @param attributeName    the attribute name
     * @return the attribute key value by name
     */
    public static AttributeKeyAndValue getAttributeKeyValueByName(final ListObjectAttributesResult attributesResult,
                                                                   final String attributeName) {
        return attributesResult.getAttributes().stream()
                .filter(a -> a.getKey().getName().equalsIgnoreCase(attributeName))
                .findFirst()
                .orElse(null);
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
        return new ListObjectAttributesRequest()
                .withDirectoryArn(arnName)
                .withObjectReference(getObjectRefById(objectId));
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
        return new ObjectReference().withSelector(path);
    }

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
                                                        final CloudDirectoryProperties cloud) {
        final ObjectAttributeRange range = getObjectAttributeRanges(cloud.getSchemaArn(), cloud.getFacetName(),
                attributeName, attributeValue);

        return new ListIndexRequest().withDirectoryArn(cloud.getDirectoryArn())
                .withIndexReference(reference)
                .withRangesOnIndexedValues(range);
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

        final AttributeKey attributeKey = getAttributeKey(schemaArn, facetName, attributeName);
        return new ObjectAttributeRange().withAttributeKey(attributeKey)
                .withRange(new TypedAttributeValueRange()
                        .withStartValue(getTypedAttributeValue(attributeValue))
                        .withEndValue(getTypedAttributeValue(attributeValue))
                        .withStartMode(RangeMode.INCLUSIVE.toString())
                        .withEndMode(RangeMode.INCLUSIVE.toString()));
    }

    /**
     * Gets attribute key.
     *
     * @param schemaArn     the schema arn
     * @param facetName     the facet name
     * @param attributeName the attribute name
     * @return the attribute key
     */
    private static AttributeKey getAttributeKey(final String schemaArn, final String facetName, final String attributeName) {
        return new AttributeKey().withFacetName(facetName).withSchemaArn(schemaArn).withName(attributeName);
    }

    /**
     * Gets typed attribute value.
     *
     * @param attributeValue the attribute value
     * @return the typed attribute value
     */
    private static TypedAttributeValue getTypedAttributeValue(final String attributeValue) {
        return new TypedAttributeValue().withStringValue(attributeValue);
    }
}
