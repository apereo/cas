package org.apereo.cas.ticket.registry;

import module java.base;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.Topic;

/**
 * This is {@link GoogleCloudPubSubMessageContext}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public record GoogleCloudPubSubMessageContext(Topic topic, Subscription subscription, Subscriber subscriber) {
}
