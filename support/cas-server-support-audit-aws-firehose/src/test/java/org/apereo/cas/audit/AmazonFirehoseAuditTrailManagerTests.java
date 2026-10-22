package org.apereo.cas.audit;

import module java.base;
import org.apereo.cas.audit.spi.AuditActionContextJsonSerializer;
import org.apereo.cas.config.CasSupportAmazonFirehoseAuditAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.audit.AuditAmazonFirehoseProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.common.web.ClientInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.services.firehose.FirehoseClient;
import software.amazon.awssdk.services.firehose.model.PutRecordRequest;
import software.amazon.awssdk.services.firehose.model.PutRecordResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AmazonFirehoseAuditTrailManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasSupportAmazonFirehoseAuditAutoConfiguration.class,
    properties = {
        "cas.audit.amazon-firehose.endpoint=http://127.0.0.1:4566",
        "cas.audit.amazon-firehose.region=us-east-1",
        "cas.audit.amazon-firehose.delivery-stream-name=security-lake-stream",
        "cas.audit.amazon-firehose.asynchronous=false"
    })
@Tag("AmazonWebServices")
@EnabledIfListeningOnPort(port = 4566)
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class AmazonFirehoseAuditTrailManagerTests {
    @Autowired
    @Qualifier("awsFirehoseAuditTrailManager")
    private AuditTrailManager auditTrailManager;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    static {
        System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), "AKIAIPPIGGUNIO74C63Z");
        System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), "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");
    }
    
    @Test
    void verifyOperation() {
        val audit = new AuditActionContext("casuser", "resource", "action",
            "CAS", LocalDateTime.now(Clock.systemUTC()),
            new ClientInfo("123.456.789.000", "123.456.789.000",
                "GoogleChrome", "London"));
        auditTrailManager.record(audit);
    }

    @Test
    void verifyRecordsAreNewlineDelimited() {
        val firehoseClient = mock(FirehoseClient.class);
        when(firehoseClient.putRecord(any(PutRecordRequest.class))).thenReturn(PutRecordResponse.builder().recordId("1").build());
        val properties = new AuditAmazonFirehoseProperties().setDeliveryStreamName("security-lake-stream").setAsynchronous(false);
        val manager = new AmazonFirehoseAuditTrailManager(firehoseClient, new AuditActionContextJsonSerializer(applicationContext), properties);
        val audit = new AuditActionContext("casuser", "resource", "action",
            "CAS", LocalDateTime.now(Clock.systemUTC()),
            new ClientInfo("127.0.0.1", "127.0.0.1", "GoogleChrome", "London"));
        manager.record(audit);
        manager.record(audit);
        val captor = ArgumentCaptor.forClass(PutRecordRequest.class);
        verify(firehoseClient, times(2)).putRecord(captor.capture());
        val delivered = captor.getAllValues().stream().map(request -> request.record().data().asUtf8String()).collect(Collectors.joining());
        val lines = delivered.split("\n");
        assertEquals(2, lines.length);
        Arrays.stream(lines).forEach(line -> assertTrue(line.contains("casuser")));
    }
}
