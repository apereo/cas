package org.apereo.cas.audit.spi;

import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.common.web.ClientInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AuditActionContextJsonSerializerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("Audits")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
class AuditActionContextJsonSerializerTests {
    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyOperation() {
        val ctx = new AuditActionContext("casuser", "TEST", "TEST",
            "CAS", LocalDateTime.now(Clock.systemUTC()),
            new ClientInfo("1.2.3.4", "1.2.3.4", UUID.randomUUID().toString(), "London"));
        val serializer = new AuditActionContextJsonSerializer(applicationContext);
        val result = serializer.toString(ctx);
        assertNotNull(result);
        val audit = serializer.from(result);
        assertNotNull(audit);
    }
}
