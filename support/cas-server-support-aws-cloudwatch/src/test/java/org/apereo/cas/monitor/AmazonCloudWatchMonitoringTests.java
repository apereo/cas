package org.apereo.cas.monitor;

import module java.base;
import org.apereo.cas.config.CasAmazonCloudWatchAutoConfiguration;
import org.apereo.cas.config.CasAmazonCoreAutoConfiguration;
import org.apereo.cas.config.CasMetricsAutoConfiguration;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import software.amazon.awssdk.core.SdkSystemSetting;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AmazonCloudWatchMonitoringTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("AmazonWebServices")
@EnabledIfListeningOnPort(port = 4566)
@SpringBootTestAutoConfigurations
@SpringBootTest(
    classes = {
        CasMetricsAutoConfiguration.class,
        CasAmazonCoreAutoConfiguration.class,
        CasAmazonCloudWatchAutoConfiguration.class
    },
    properties = "management.cloudwatch.metrics.export.enabled=true")
class AmazonCloudWatchMonitoringTests {
    static {
        System.setProperty(SdkSystemSetting.AWS_ACCESS_KEY_ID.property(), "AKIAIPPIGGUNIO74C63Z");
        System.setProperty(SdkSystemSetting.AWS_SECRET_ACCESS_KEY.property(), "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");
        System.setProperty(SdkSystemSetting.AWS_SESSION_TOKEN.property(), UUID.randomUUID().toString());
        System.setProperty("aws.cloudwatch.endpoint", "http://127.0.0.1:4566");
    }

    @Autowired
    @Qualifier("cloudWatchMeterRegistry")
    private MeterRegistry cloudWatchMeterRegistry;

    @Test
    void verifyOperation() {
        val startTime = System.currentTimeMillis();
        val execTimer = cloudWatchMeterRegistry.timer("cas.test.Execution", "tag1", "tag2");
        assertEquals(HttpStatus.OK, HttpRequestUtils.pingUrl("http://127.0.0.1:4566/_localstack/health"));
        execTimer.record(Duration.ofMillis(System.currentTimeMillis() - startTime));
    }
}
