package org.apereo.cas.logging;

import org.apereo.cas.config.CasAmazonCloudWatchAutoConfiguration;
import org.apereo.cas.config.CasAmazonCoreAutoConfiguration;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.core.SdkSystemSetting;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CloudWatchLogsEndpointTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("AmazonWebServices")
@EnabledIfListeningOnPort(port = 4566)
@SpringBootTestAutoConfigurations
@SpringBootTest(
    classes = {
        CasAmazonCoreAutoConfiguration.class,
        CasAmazonCloudWatchAutoConfiguration.class
    },
    properties = {
        "cas.logging.cloud-watch.endpoint=http://127.0.0.1:4566",
        "cas.logging.cloud-watch.region=us-east-1",
        "cas.logging.cloud-watch.credential-access-key=test",
        "cas.logging.cloud-watch.credential-secret-key=test",
        "cas.logging.cloud-watch.log-group-name=cas-log-group",
        "cas.logging.cloud-watch.log-stream-name=cas-log-stream",

        "management.endpoint.cloudWatchLogs.access=UNRESTRICTED",
        "management.endpoints.web.exposure.include=*"
    })
class CloudWatchLogsEndpointTests {
    static {
        System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), "AKIAIPPIGGUNIO74C63Z");
        System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), "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");
        System.setProperty(SdkSystemSetting.AWS_SESSION_TOKEN.property(), UUID.randomUUID().toString());
    }

    @Autowired
    @Qualifier("cloudWatchLogsEndpoint")
    private CloudWatchLogsEndpoint cloudWatchLogsEndpoint;

    @Test
    void verifyOperation() {
        val events = cloudWatchLogsEndpoint.fetchLogEntries(20, "info");
        assertFalse(events.isEmpty());
    }
}
