package org.apereo.cas.tomcat;

import org.apereo.cas.CasEmbeddedContainerUtils;
import org.apereo.cas.config.CasEmbeddedContainerTomcatConfiguration;
import org.apereo.cas.config.CasEmbeddedContainerTomcatFiltersConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

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
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@EnableConfigurationProperties({CasConfigurationProperties.class, ServerProperties.class})
@TestPropertySource(properties = {
    "server.port=8182",
    "server.ssl.enabled=false",
    "cas.server.tomcat.clustering.sessionClusteringEnabled=false",
    "cas.server.tomcat.sslValve.enabled=true",
    "cas.server.tomcat.httpProxy.enabled=true",
    "cas.server.tomcat.httpProxy.secure=true",
    "cas.server.tomcat.httpProxy.scheme=https",
    "cas.server.tomcat.http.enabled=true",
    "cas.server.tomcat.http.port=9190",
    "cas.server.tomcat.ajp.enabled=true",
    "cas.server.tomcat.ajp.port=9944",
    "cas.server.tomcat.basicAuthn.enabled=true",
    "cas.server.tomcat.extAccessLog.enabled=true",
    "cas.server.tomcat.rewriteValve.location=classpath:/container/tomcat/rewrite.config"
})
@Slf4j
public class CasTomcatServletWebServerFactoryTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casServletWebServerFactory")
    private ConfigurableServletWebServerFactory casServletWebServerFactory;

    @Autowired
    @Qualifier("casTomcatEmbeddedServletContainerCustomizer")
    private ServletWebServerFactoryCustomizer casTomcatEmbeddedServletContainerCustomizer;

    static {
        System.setProperty(CasEmbeddedContainerUtils.EMBEDDED_CONTAINER_CONFIG_ACTIVE, "true");
    }

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
