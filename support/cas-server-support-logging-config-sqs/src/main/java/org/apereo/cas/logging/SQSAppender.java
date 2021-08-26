package org.apereo.cas.logging;

import org.apereo.cas.aws.ChainingAWSCredentialsProvider;
import org.apereo.cas.util.CollectionUtils;

import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This is {@link SQSAppender}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Plugin(name = "SQSAppender", category = "Core", elementType = "appender", printObject = true)
public class SQSAppender extends AbstractAppender implements Serializable {
    private static final long serialVersionUID = 1144758913028847477L;

    private final SqsAsyncClient sqsAsyncClient;

    private final String queueName;

    private final Map<String, String> queueTags;

    private String queueUrl;

    @SneakyThrows
    public SQSAppender(final String name,
                       final Layout<Serializable> layout,
                       final String credentialAccessKey,
                       final String credentialSecretKey,
                       final String awsLogRegionName,
                       final String endpoint,
                       final String queueName,
                       final String queueTags) {
        super(name, null, layout == null
            ? PatternLayout.createDefaultLayout()
            : layout, false, Property.EMPTY_ARRAY);

        val builder = SqsAsyncClient.builder();
        if (StringUtils.isNotBlank(endpoint)) {
            builder.endpointOverride(new URI(endpoint));
        }
        builder.region(Region.of(awsLogRegionName));
        builder.credentialsProvider(ChainingAWSCredentialsProvider.getInstance(credentialAccessKey, credentialSecretKey));
        this.sqsAsyncClient = builder.build();
        this.queueName = queueName;

        val tags = org.springframework.util.StringUtils.commaDelimitedListToSet(queueTags);
        this.queueTags = CollectionUtils.convertDirectedListToMap(tags);
    }

    /**
     * Create appender.
     *
     * @param name                the name
     * @param credentialAccessKey the credential access key
     * @param credentialSecretKey the credential secret key
     * @param region              the aws log region name
     * @param endpoint            the endpoint
     * @param queueName           the queue name
     * @param queueTags           the queue Tags
     * @param layout              the layout
     * @return the sqs appender
     */
    @PluginFactory
    public static SQSAppender createAppender(@PluginAttribute("name") final String name,
                                             @PluginAttribute(value = "credentialAccessKey", sensitive = true) final String credentialAccessKey,
                                             @PluginAttribute(value = "credentialSecretKey", sensitive = true) final String credentialSecretKey,
                                             @PluginAttribute("region") final String region,
                                             @PluginAttribute("endpoint") final String endpoint,
                                             @PluginAttribute("queueName") final String queueName,
                                             @PluginAttribute("queueTags") final String queueTags,
                                             @PluginElement("Layout") final Layout<Serializable> layout) {
        return new SQSAppender(
            name,
            layout,
            StringUtils.defaultIfBlank(credentialAccessKey, System.getProperty("AWS_ACCESS_KEY")),
            StringUtils.defaultIfBlank(credentialSecretKey, System.getProperty("AWS_SECRET_KEY")),
            StringUtils.defaultIfBlank(region, System.getProperty("AWS_REGION_NAME")),
            endpoint,
            queueName,
            queueTags);
    }

    @Override
    @SneakyThrows
    public void start() {
        try {
            val request = GetQueueUrlRequest.builder()
                .queueName(this.queueName)
                .build();
            val response = sqsAsyncClient.getQueueUrl(request).get();
            this.queueUrl = response.queueUrl();
        } catch (final Exception e) {
            LOGGER.debug("Failed to fetch queue", e);
            val request = CreateQueueRequest.builder()
                .queueName(this.queueName)
                .tags(this.queueTags)
                .build();
            val response = sqsAsyncClient.createQueue(request).get();
            this.queueUrl = response.queueUrl();
            LOGGER.debug("Created queue URL is [{}]", this.queueUrl);
        }
        super.start();
    }

    @SneakyThrows
    @Override
    public void append(final LogEvent event) {
        val context = event.getContextData()
            .toMap()
            .entrySet()
            .stream()
            .map(entry -> Pair.of(entry.getKey(), MessageAttributeValue.builder().stringValue(entry.getValue()).build()))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        val message = new String(getLayout().toByteArray(event), StandardCharsets.UTF_8);
        val request = SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody(message)
            .messageAttributes(context)
            .build();
        sqsAsyncClient.sendMessage(request).get();
    }

    @Override
    public boolean stop(final long timeout, final TimeUnit timeUnit) {
        sqsAsyncClient.close();
        return super.stop(timeout, timeUnit, false);
    }
}
