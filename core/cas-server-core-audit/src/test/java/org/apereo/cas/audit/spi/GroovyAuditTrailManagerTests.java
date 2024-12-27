package org.apereo.cas.audit.spi;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.common.web.ClientInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import static org.mockito.Mockito.*;

/**
 * This is {@link GroovyAuditTrailManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Audits")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    BaseAuditConfigurationTests.SharedTestConfiguration.class,
    GroovyAuditTrailManagerTests.GroovyAuditTrailManagerTestConfiguration.class
},
    properties = {
        "cas.audit.slf4j.enabled=false",
        "cas.audit.groovy.template.location=classpath:/GroovyAudit.groovy"
    })
@EnableConfigurationProperties(CasConfigurationProperties.class)
class GroovyAuditTrailManagerTests {

    @Autowired
    @Qualifier("filterAndDelegateAuditTrailManager")
    private AuditTrailManager auditTrailManager;

    @Test
    void verifyOperation() {
        val clientInfo = new ClientInfo("1.2.3.4", "1.2.3.4", UUID.randomUUID().toString(), "London")
            .setExtraInfo(Map.of("Hello", "World"))
            .setHeaders(Map.of("H1", "V1"));
        val ctx = new AuditActionContext("casuser",
            "TEST", "TEST", "CAS", LocalDateTime.now(Clock.systemUTC()),
            clientInfo);
        auditTrailManager.record(ctx);
    }

    @TestConfiguration(value = "GroovyAuditTrailManagerTestConfiguration", proxyBeanMethods = false)
    static class GroovyAuditTrailManagerTestConfiguration {
        @Bean
        public GeoLocationService geoLocationService() {
            val mock = mock(GeoLocationService.class);
            when(mock.locate(anyString())).thenReturn(new GeoLocationResponse()
                .setLatitude(156)
                .setLongitude(34)
                .addAddress("London, UK"));
            return mock;
        }
    }
}
