package org.apereo.cas.support.sms;

import org.apereo.cas.config.SmsModeSmsConfiguration;
import org.apereo.cas.notifications.sms.SmsSender;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.io.IOException;
import java.net.InetSocketAddress;

import static java.nio.charset.StandardCharsets.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SmsModeSmsSenderTests}.
 *
 * @author Jérôme Rautureau
 * @since 6.5.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    SmsModeSmsConfiguration.class
},
    properties = "cas.sms-provider.sms-mode.url=http://localhost:8099")
@Tag("SMS")
public class SmsModeSmsSenderTests {
    @Autowired
    @Qualifier(SmsSender.BEAN_NAME)
    private SmsSender smsSender;

    @Test
    public void verifyOperation() throws IOException {
        assertNotNull(smsSender);
        assertFalse(smsSender.send("123-456-7890", "123-456-7890", "TEST"));
        val server = HttpServer.create(new InetSocketAddress(8099), 0);
        try {
            server.createContext("/", new SmsModeHandler());
            server.setExecutor(null);
            server.start();
            assertTrue(smsSender.send("123-456-7890", "123-456-7890", "TEST"));
        } finally {
            server.stop(5);
        }
    }

    private static class SmsModeHandler implements HttpHandler {
        @Override
        public void handle(final HttpExchange exchange) throws IOException {
            val uri = exchange.getRequestURI().toString();
            var response = "0";
            if (!uri.contains("accessToken")) {
                response = "35";
            }
            exchange.sendResponseHeaders(200, response.length());
            val os = exchange.getResponseBody();
            os.write(response.getBytes(UTF_8));
            os.close();
        }
    }
}
