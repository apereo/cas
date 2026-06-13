package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;
import org.apereo.cas.util.serialization.StringSerializer;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.pubsub.v1.PubsubMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link CasGoogleCloudServiceRegistryMessageReceiver}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class CasGoogleCloudServiceRegistryMessageReceiver implements MessageReceiver {
    private final ServiceRegistry serviceRegistry;
    private final Storage storage;
    private final StringSerializer<RegisteredService> serializer;
    private final RegisteredServiceResourceNamingStrategy namingStrategy;

    @Override
    public void receiveMessage(final PubsubMessage message, final AckReplyConsumer consumer) {
        val eventType = message.getAttributesOrDefault("eventType", StringUtils.EMPTY);
        val bucketId = message.getAttributesOrDefault("bucketId", StringUtils.EMPTY);
        val objectId = message.getAttributesOrDefault("objectId", StringUtils.EMPTY);
        val generation = message.getAttributesOrDefault("objectGeneration", StringUtils.EMPTY);
        LOGGER.debug("Event received: [{}] bucket=[{}] object[{}] generation=[{}]",
            eventType, bucketId, objectId, generation);

        if (EventTypes.OBJECT_FINALIZE.name().equalsIgnoreCase(eventType)) {
            val blob = storage.get(BlobId.of(bucketId, objectId));
            if (blob != null) {
                val content = new String(blob.getContent(), StandardCharsets.UTF_8);
                handleUpdatedObject(bucketId, objectId, content);
            }
        }

        if (EventTypes.OBJECT_DELETE.name().equalsIgnoreCase(eventType)) {
            handleDeletedObject(bucketId, objectId);
        }
        consumer.ack();
    }

    protected void handleUpdatedObject(final String bucket, final String objectName, final String content) {
        //CHECKSTYLE:OFF
        LOGGER.trace("Updating object: gs://{}/{}", bucket, objectName);
        //CHECKSTYLE:ON
        val registeredService = serializer.from(content);
        serviceRegistry.save(registeredService);
    }

    protected void handleDeletedObject(final String bucket, final String objectName) {
        val id = namingStrategy.extractServiceId(objectName);
        val registeredService = serviceRegistry.findServiceById(id);
        if (registeredService != null) {
            //CHECKSTYLE:OFF
            LOGGER.debug("Deleting object: gs://{}/{}", bucket, objectName);
            val result = serviceRegistry.delete(registeredService);
            LOGGER.debug("Deleted object: gs://{}/{} => {}", bucket, objectName, BooleanUtils.toStringYesNo(result));
            //CHECKSTYLE:ON
        }
    }

    /**
     * The enum event types.
     */
    public enum EventTypes {
        /**
         * Object finalize event types.
         */
        OBJECT_FINALIZE,
        /**
         * Object delete event types.
         */
        OBJECT_DELETE
    }
}
