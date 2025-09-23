package org.apereo.cas.logging;

import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasGoogleCloudLoggingAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleCloudAppenderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Simple")
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasGoogleCloudLoggingAutoConfiguration.class
}, properties = "cas.logging.gcp.project-id=${random.uuid}")
@EnableConfigurationProperties(CasConfigurationProperties.class)
class GoogleCloudAppenderTests {

    @Autowired
    @Qualifier("googleCloudLoggingInterceptor")
    private HandlerInterceptor googleCloudLoggingInterceptor;

    @Test
    void verifyOperation() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setPreferredLocales(List.of(Locale.FRENCH));
        request.setRequestURI("/login");
        request.setMethod("POST");
        request.setQueryString("one=two");
        request.addHeader(GoogleCloudLoggingWebInterceptor.HEADER_B3_TRACE_ID, UUID.randomUUID().toString());
        val response = new MockHttpServletResponse();
        ThreadContext.put("protocol", "HTTP 1.1");
        ThreadContext.put(HttpHeaders.USER_AGENT, "Firefox");
        ThreadContext.put("remoteIp", "127.0.0.1");
        ThreadContext.put("method", request.getMethod());
        googleCloudLoggingInterceptor.preHandle(request, response, this);

        val context = LoggerContext.getContext(false);
        val logger = context.getLogger(GoogleCloudAppender.class.getName());
        logger.info("This is an INFO log message here");
        logger.warn("This is a WARNING log message here");
        logger.info("This is a parametrized message with a POJO [{}]",
            new Pojo(UUID.randomUUID().toString(), 1984L));
        logger.info(new Pojo("Payload is an object here", 1984L));
        logger.info(Map.of("application", "CAS", "org", "Apereo"));
        logger.info("Hello World [{}]", Map.of("application", "CAS", "org", "Apereo"));

        val appender = (GoogleCloudAppender) logger.getAppenders().get("GoogleCloudAppender");
        assertNotNull(appender);

        context.stop(5, TimeUnit.SECONDS);
    }

    public record Pojo(String name, Long id) {
    }
}
