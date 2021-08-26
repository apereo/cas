package org.apereo.cas.clouddirectory;

import org.apereo.cas.configuration.model.support.clouddirectory.AmazonCloudDirectoryProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import software.amazon.awssdk.services.clouddirectory.CloudDirectoryClient;
import software.amazon.awssdk.services.clouddirectory.model.ListIndexRequest;
import software.amazon.awssdk.services.clouddirectory.model.ListIndexResponse;
import software.amazon.awssdk.services.clouddirectory.model.ListObjectAttributesRequest;
import software.amazon.awssdk.services.clouddirectory.model.ObjectReference;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultAmazonCloudDirectoryRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultAmazonCloudDirectoryRepository implements AmazonCloudDirectoryRepository {
    private final CloudDirectoryClient amazonCloudDirectory;

    private final AmazonCloudDirectoryProperties properties;

    @Override
    public Map<String, List<Object>> getUser(final String username) {
        val indexResult = getIndexResult(username);
        if (indexResult == null) {
            LOGGER.warn("Index result could not be found for user [{}]", username);
            return new LinkedHashMap<>();
        }
        return getUserInfoFromIndexResult(indexResult);
    }

    /**
     * Gets list index request.
     *
     * @param username  the username
     * @param reference the reference
     * @return the list index request
     */
    protected ListIndexRequest getListIndexRequest(final String username, final ObjectReference reference) {
        return AmazonCloudDirectoryUtils.getListIndexRequest(
            properties.getUsernameAttributeName(),
            username, reference, properties);
    }

    /**
     * Gets index result.
     *
     * @param username the username
     * @return the index result
     */
    protected ListIndexResponse getIndexResult(final String username) {
        val reference = getObjectReference();
        if (reference != null) {
            val listIndexRequest = getListIndexRequest(username, reference);
            if (listIndexRequest != null) {
                return amazonCloudDirectory.listIndex(listIndexRequest);
            }
        }
        LOGGER.warn("Object reference or list index request could not be found for user [{}]", username);
        return null;
    }

    /**
     * Gets object reference.
     *
     * @return the object reference
     */
    protected ObjectReference getObjectReference() {
        return AmazonCloudDirectoryUtils.getObjectRefByPath(properties.getUsernameIndexPath());
    }

    /**
     * Gets user info from index result.
     *
     * @param indexResult the index result
     * @return the user info from index result
     */
    protected Map<String, List<Object>> getUserInfoFromIndexResult(final ListIndexResponse indexResult) {
        val attachment = indexResult.indexAttachments().stream().findFirst().orElse(null);
        if (attachment == null) {
            LOGGER.warn("Index result has no attachments");
            return null;
        }

        val identifier = attachment.objectIdentifier();
        val listObjectAttributesRequest = getListObjectAttributesRequest(identifier);
        if (listObjectAttributesRequest == null) {
            LOGGER.warn("No object attribute request is available for identifier [{}]", identifier);
            return null;
        }
        val attributesResult = amazonCloudDirectory.listObjectAttributes(listObjectAttributesRequest);
        if (attributesResult == null || !attributesResult.hasAttributes()) {
            LOGGER.warn("No object attribute result is available for identifier [{}] or not attributes are found", identifier);
            return null;
        }

        return attributesResult.attributes()
            .stream()
            .map(a -> {
                var value = (Object) null;
                val attributeValue = a.value();
                LOGGER.debug("Examining attribute [{}]", a);
                if (StringUtils.isNotBlank(attributeValue.numberValue())) {
                    value = attributeValue.numberValue();
                } else if (attributeValue.datetimeValue() != null) {
                    value = DateTimeUtils.zonedDateTimeOf(attributeValue.datetimeValue()).toString();
                } else if (attributeValue.booleanValue() != null) {
                    value = attributeValue.booleanValue().toString();
                } else if (attributeValue.binaryValue() != null) {
                    value = new String(attributeValue.binaryValue().asByteArray(), StandardCharsets.UTF_8);
                } else if (StringUtils.isNotBlank(attributeValue.stringValue())) {
                    value = attributeValue.stringValue();
                }
                return Pair.of(a.key().name(), value);
            })
            .filter(p -> p.getValue() != null)
            .collect(Collectors.toMap(Pair::getKey, s -> CollectionUtils.toCollection(s.getValue(), ArrayList.class)));
    }

    /**
     * Gets list object attributes request.
     *
     * @param identifier the identifier
     * @return the list object attributes request
     */
    protected ListObjectAttributesRequest getListObjectAttributesRequest(final String identifier) {
        return AmazonCloudDirectoryUtils.getListObjectAttributesRequest(properties.getDirectoryArn(), identifier);
    }
}
