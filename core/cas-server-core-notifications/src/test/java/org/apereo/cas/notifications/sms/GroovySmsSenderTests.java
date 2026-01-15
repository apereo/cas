package org.apereo.cas.notifications.sms;

import module java.base;
import org.apereo.cas.notifications.BaseNotificationTests;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovySmsSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = BaseNotificationTests.SharedTestConfiguration.class,
    properties = "cas.sms-provider.groovy.location=classpath:/GroovySmsSender.groovy")
@Tag("Groovy")
@ExtendWith(CasTestExtension.class)
class GroovySmsSenderTests {
    @Autowired
    @Qualifier(CommunicationsManager.BEAN_NAME)
    private CommunicationsManager communicationsManager;

    @Test
    void verifyOperation() {
        assertTrue(communicationsManager.isSmsSenderDefined());
        val smsRequest = SmsRequest.builder().from("CAS")
            .to(List.of("1234567890")).text("Hello CAS").build();
        assertTrue(communicationsManager.sms(smsRequest));
    }
}
