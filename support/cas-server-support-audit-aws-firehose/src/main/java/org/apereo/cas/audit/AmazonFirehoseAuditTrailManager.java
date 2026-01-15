package org.apereo.cas.audit;

import module java.base;
import org.apereo.cas.audit.spi.AbstractAuditTrailManager;
import org.apereo.cas.audit.spi.AuditActionContextJsonSerializer;
import org.apereo.cas.configuration.model.core.audit.AuditAmazonFirehoseProperties;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.firehose.FirehoseClient;
import software.amazon.awssdk.services.firehose.model.PutRecordRequest;
import software.amazon.awssdk.services.firehose.model.Record;

/**
 * This is {@link AmazonFirehoseAuditTrailManager}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
public class AmazonFirehoseAuditTrailManager extends AbstractAuditTrailManager {

    private final FirehoseClient firehoseClient;
    private final AuditActionContextJsonSerializer serializer;
    private final AuditAmazonFirehoseProperties properties;

    public AmazonFirehoseAuditTrailManager(final FirehoseClient firehoseClient,
                                           final AuditActionContextJsonSerializer serializer,
                                           final AuditAmazonFirehoseProperties properties) {
        super(properties.isAsynchronous());
        this.firehoseClient = firehoseClient;
        this.serializer = serializer;
        this.properties = properties;
    }

    @Override
    protected void saveAuditRecord(final AuditActionContext audit) {
        val eventJson = serializer.toString(audit);
        val record = Record.builder()
            .data(SdkBytes.fromUtf8String(eventJson))
            .build();
        val request = PutRecordRequest.builder()
            .deliveryStreamName(properties.getDeliveryStreamName())
            .record(record)
            .build();
        val response = firehoseClient.putRecord(request);
        LOGGER.debug("Sent audit event to Firehose delivery stream [{}] and received record ID [{}]",
            properties.getDeliveryStreamName(), response.recordId());
    }
}
