package org.apereo.cas.clouddirectory;

import com.amazonaws.services.clouddirectory.AmazonCloudDirectory;
import com.amazonaws.services.clouddirectory.model.ListIndexResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.configuration.model.support.clouddirectory.CloudDirectoryProperties;
import org.apereo.cas.util.DateTimeUtils;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultCloudDirectoryRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultCloudDirectoryRepository implements CloudDirectoryRepository {
    private final AmazonCloudDirectory amazonCloudDirectory;
    private final CloudDirectoryProperties properties;

    @Override
    public Map<String, Object> getUser(final String username) {
        final var indexResult = getIndexResult(username);
        if (indexResult == null) {
            LOGGER.warn("Index result could not be found for user [{}]", username);
            return new LinkedHashMap<>();
        }
        return getUserInfoFromIndexResult(indexResult);
    }

    private ListIndexResult getIndexResult(final String username) {
        final var reference = CloudDirectoryUtils.getObjectRefByPath(properties.getUsernameIndexPath());
        if (reference != null) {
            final var listIndexRequest = CloudDirectoryUtils.getListIndexRequest(
                properties.getUsernameAttributeName(),
                username, reference, properties);

            if (listIndexRequest != null) {
                return amazonCloudDirectory.listIndex(listIndexRequest);
            }
        }
        LOGGER.warn("Object reference or list index request could not be found for user [{}]", username);
        return null;
    }

    /**
     * Gets user info from index result.
     *
     * @param indexResult the index result
     * @return the user info from index result
     */
    protected Map<String, Object> getUserInfoFromIndexResult(final ListIndexResult indexResult) {
        final var attachment = indexResult.getIndexAttachments().stream().findFirst().orElse(null);
        if (attachment == null) {
            LOGGER.warn("Index result has no attachments");
            return null;
        }

        final var identifier = attachment.getObjectIdentifier();
        final var listObjectAttributesRequest = CloudDirectoryUtils.getListObjectAttributesRequest(properties.getDirectoryArn(), identifier);
        if (listObjectAttributesRequest == null) {
            LOGGER.warn("No object attribute request is available for identifier [{}]", identifier);
            return null;
        }
        final var attributesResult = amazonCloudDirectory.listObjectAttributes(listObjectAttributesRequest);
        if (attributesResult == null || attributesResult.getAttributes() == null || attributesResult.getAttributes().isEmpty()) {
            LOGGER.warn("No object attribute result is available for identifier [{}] or not attributes are found", identifier);
            return null;
        }

        return attributesResult.getAttributes()
            .stream()
            .map(a -> {
                Object value = null;
                final var attributeValue = a.getValue();
                LOGGER.debug("Examining attribute [{}]", a);

                if (StringUtils.isNotBlank(attributeValue.getNumberValue())) {
                    value = attributeValue.getNumberValue();
                } else if (attributeValue.getDatetimeValue() != null) {
                    value = DateTimeUtils.zonedDateTimeOf(attributeValue.getDatetimeValue()).toString();
                } else if (attributeValue.getBooleanValue() != null) {
                    value = attributeValue.getBooleanValue().toString();
                } else if (attributeValue.getBinaryValue() != null) {
                    value = new String(attributeValue.getBinaryValue().array(), StandardCharsets.UTF_8);
                } else if (StringUtils.isNotBlank(attributeValue.getStringValue())) {
                    value = attributeValue.getStringValue();
                }

                return Pair.of(a.getKey().getName(), value);
            })
            .filter(p -> p.getValue() != null)
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }
}
