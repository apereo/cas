package org.apereo.cas.tomcat;

import org.apereo.cas.config.CasEmbeddedContainerTomcatConfiguration;
import org.apereo.cas.config.CasEmbeddedContainerTomcatFiltersConfiguration;
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
@SpringBootTest(classes = {
    CasEmbeddedContainerTomcatConfiguration.class,
    CasEmbeddedContainerTomcatFiltersConfiguration.class
},
    properties = {
        "server.port=8182",
        "server.ssl.enabled=false",
        "cas.server.tomcat.socket.bufferPool=10",
        "cas.server.tomcat.socket.appReadBufSize=10",
        "cas.server.tomcat.socket.appWriteBufSize=10",
        "cas.server.tomcat.socket.performanceBandwidth=10",
        "cas.server.tomcat.socket.performanceConnectionTime=1000",
        "cas.server.tomcat.socket.performanceLatency=10",
        "cas.server.tomcat.apr.enabled=true",
        "cas.server.tomcat.sslValve.enabled=true",
        "cas.server.tomcat.httpProxy.enabled=true",
        "cas.server.tomcat.httpProxy.secure=true",
        "cas.server.tomcat.httpProxy.scheme=https",
        "cas.server.tomcat.httpProxy.secret=s3cr3t",
        "cas.server.tomcat.httpProxy.redirectPort=1234",
        "cas.server.tomcat.httpProxy.proxyPort=1212",
        "cas.server.tomcat.http.enabled=true",
        "cas.server.tomcat.http.port=9190",
        "cas.server.tomcat.ajp.enabled=true",
        "cas.server.tomcat.ajp.port=9944",
        "cas.server.tomcat.ajp.secret=s3cr3t",
        "cas.server.tomcat.ajp.redirectPort=1234",
        "cas.server.tomcat.ajp.proxyPort=1212",
        "cas.server.tomcat.basicAuthn.enabled=true",
        "cas.server.tomcat.extAccessLog.enabled=true",
        "cas.server.tomcat.rewriteValve.location=classpath:/container/tomcat/rewrite.config"
    },
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@EnableConfigurationProperties({CasConfigurationProperties.class, ServerProperties.class})
@Tag("Simple")
public class CasTomcatServletWebServerFactoryTests {
    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casServletWebServerFactory")
    private ConfigurableServletWebServerFactory casServletWebServerFactory;

    @Autowired
    @Qualifier("casTomcatEmbeddedServletContainerCustomizer")
    private ServletWebServerFactoryCustomizer casTomcatEmbeddedServletContainerCustomizer;


    @Test
    public void verifyOperation() {
        casTomcatEmbeddedServletContainerCustomizer.customize(casServletWebServerFactory);
        val server = casServletWebServerFactory.getWebServer();
        try {
            server.start();
        } finally {
            server.stop();
        }
    }
}
