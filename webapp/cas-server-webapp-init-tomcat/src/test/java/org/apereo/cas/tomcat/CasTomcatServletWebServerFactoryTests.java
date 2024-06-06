package org.apereo.cas.tomcat;

import org.apereo.cas.config.CasEmbeddedContainerTomcatAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;

/**
 * This is {@link CasTomcatServletWebServerFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(
    classes = CasEmbeddedContainerTomcatAutoConfiguration.class,
    properties = {
        "server.port=8182",
        "server.ssl.enabled=false",
        "server.servlet.context-path=/cas",

        "cas.server.tomcat.socket.buffer-pool=10",
        "cas.server.tomcat.socket.app-read-buf-size=10",
        "cas.server.tomcat.socket.app-write-buf-size=10",
        "cas.server.tomcat.socket.performance-bandwidth=10",
        "cas.server.tomcat.socket.performance-connection-time=1000",
        "cas.server.tomcat.socket.performance-latency=10",

        "cas.server.tomcat.ssl-valve.enabled=true",

        "cas.server.tomcat.http-proxy.enabled=true",
        "cas.server.tomcat.http-proxy.secure=true",
        "cas.server.tomcat.http-proxy.scheme=https",
        "cas.server.tomcat.http-proxy.secret=s3cr3t",
        "cas.server.tomcat.http-proxy.redirect-port=1234",
        "cas.server.tomcat.http-proxy.proxy-port=1212",

        "cas.server.tomcat.http[0].enabled=true",
        "cas.server.tomcat.http[0].port=0",
        "cas.server.tomcat.http[0].redirect-port=9890",

        "cas.server.tomcat.ajp.enabled=true",
        "cas.server.tomcat.ajp.port=9944",
        "cas.server.tomcat.ajp.secret=s3cr3t",
        "cas.server.tomcat.ajp.redirect-port=1234",
        "cas.server.tomcat.ajp.proxy-port=1212",

        "cas.server.tomcat.basic-authn.enabled=true",
        "cas.server.tomcat.ext-access-log.enabled=true",
        "cas.server.tomcat.rewrite-valve.location=classpath:/container/tomcat/rewrite.config"
    }, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@EnableConfigurationProperties({CasConfigurationProperties.class, ServerProperties.class})
@Tag("WebApp")
class CasTomcatServletWebServerFactoryTests {
    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casServletWebServerFactory")
    private ConfigurableServletWebServerFactory casServletWebServerFactory;

    @Autowired
    @Qualifier("casTomcatEmbeddedServletContainerCustomizer")
    private ServletWebServerFactoryCustomizer casTomcatEmbeddedServletContainerCustomizer;

    @Test
    void verifyOperation() {
        casTomcatEmbeddedServletContainerCustomizer.customize(casServletWebServerFactory);
        val server = casServletWebServerFactory.getWebServer();
        try {
            server.start();
        } finally {
            server.stop();
        }
    }
}
