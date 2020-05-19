package org.apereo.cas.tomcat;

import org.apereo.cas.config.CasEmbeddedContainerTomcatConfiguration;
import org.apereo.cas.config.CasEmbeddedContainerTomcatFiltersConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.apache.catalina.ha.tcp.SimpleTcpCluster;
import org.apache.catalina.tribes.group.GroupChannel;
import org.apache.catalina.tribes.membership.cloud.CloudMembershipService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This is {@link CasTomcatServletWebServerFactoryCloudClusterTests}.
 *
 * @author Hal Deadman
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    CasEmbeddedContainerTomcatConfiguration.class,
    CasEmbeddedContainerTomcatFiltersConfiguration.class
},
    properties = {
        "server.port=8183",
        "server.ssl.enabled=false",
        "cas.server.tomcat.clustering.enabled=true",
        "cas.server.tomcat.clustering.clusteringType=CLOUD"
    },
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@EnableConfigurationProperties({CasConfigurationProperties.class, ServerProperties.class})
@Tag("Simple")
public class CasTomcatServletWebServerFactoryCloudClusterTests {
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
            val tomcatServer = (TomcatWebServer) server;
            val cluster = (SimpleTcpCluster) tomcatServer.getTomcat().getEngine().getCluster();
            val channel = (GroupChannel) cluster.getChannel();
            val membership = channel.getMembershipService();
            assertTrue(membership instanceof CloudMembershipService);
        } finally {
            server.stop();
        }
    }
}

