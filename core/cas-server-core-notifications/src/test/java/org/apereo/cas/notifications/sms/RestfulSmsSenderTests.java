package org.apereo.cas.notifications.sms;

import org.apereo.cas.notifications.BaseNotificationTests;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockWebServer;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulSmsSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */

@Tag("RestfulApi")
@ExtendWith(CasTestExtension.class)
class RestfulSmsSenderTests {

    @Nested
    @SpringBootTest(classes = BaseNotificationTests.SharedTestConfiguration.class,
        properties = {
            "cas.sms-provider.rest.style=REQUEST_BODY",
            "cas.sms-provider.rest.url=http://localhost:8232"
        })

    public class RequestBody {
        @Autowired
        @Qualifier(CommunicationsManager.BEAN_NAME)
        private CommunicationsManager communicationsManager;

        private MockWebServer webServer;

        @BeforeEach
        public void initialize() {
            val request = new MockHttpServletRequest();
            request.setRemoteAddr("185.86.151.11");
            request.setLocalAddr("185.88.151.11");
            ClientInfoHolder.setClientInfo(ClientInfo.from(request));

            webServer = new MockWebServer(8232,
                new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "REST Output"),
                MediaType.APPLICATION_JSON_VALUE);
            webServer.start();
        }

        @AfterEach
        public void cleanup() {
            webServer.stop();
        }

        @Test
        void verifySms() throws Throwable {
            assertTrue(communicationsManager.isSmsSenderDefined());
            val smsRequest = SmsRequest.builder().from("CAS")
                .to(List.of("1234567890")).text("Hello CAS").build();
            assertTrue(communicationsManager.sms(smsRequest));
        }
    }

    @Nested
    @SpringBootTest(classes = BaseNotificationTests.SharedTestConfiguration.class,
        properties = {
            "cas.sms-provider.rest.style=QUERY_PARAMETERS",
            "cas.sms-provider.rest.url=http://localhost:8132"
        })

    public class RequestParameters {
        @Autowired
        @Qualifier(CommunicationsManager.BEAN_NAME)
        private CommunicationsManager communicationsManager;

        private MockWebServer webServer;

        @BeforeEach
        public void initialize() {
            val request = new MockHttpServletRequest();
            request.setRemoteAddr("185.86.151.11");
            request.setLocalAddr("185.88.151.11");
            ClientInfoHolder.setClientInfo(ClientInfo.from(request));

            webServer = new MockWebServer(8132,
                new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "REST Output"),
                MediaType.APPLICATION_JSON_VALUE);
            webServer.start();
        }

        @AfterEach
        public void cleanup() {
            webServer.stop();
        }

        @Test
        void verifySms() throws Throwable {
            assertTrue(communicationsManager.isSmsSenderDefined());
            val smsRequest = SmsRequest.builder().from("CAS")
                .to(List.of("1234567890")).text("Hello CAS").build();
            assertTrue(communicationsManager.sms(smsRequest));
        }
    }
}
