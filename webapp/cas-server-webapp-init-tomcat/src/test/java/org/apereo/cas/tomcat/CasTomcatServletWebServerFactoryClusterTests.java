package org.apereo.cas.tomcat;

import org.apereo.cas.config.CasEmbeddedContainerTomcatAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.apache.catalina.ha.tcp.SimpleTcpCluster;
import org.apache.catalina.tribes.group.GroupChannel;
import org.apache.catalina.tribes.membership.McastService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasTomcatServletWebServerFactoryClusterTests}.
 *
 * @author Hal Deadman
 * @since 6.2.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = CasEmbeddedContainerTomcatAutoConfiguration.class, properties = {
    "server.ssl.enabled=false",
    "cas.server.tomcat.remote-user-valve.remote-user-header=REMOTE_USER",
    "cas.server.tomcat.remote-user-valve.allowed-ip-address-regex=.+",
    "cas.server.tomcat.clustering.enabled=true",
    "cas.server.tomcat.clustering.clustering-type=DEFAULT"
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties({CasConfigurationProperties.class, ServerProperties.class})
@Tag("WebApp")
@ExtendWith(CasTestExtension.class)
class CasTomcatServletWebServerFactoryClusterTests {
    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("casServletWebServerFactory")
    private ConfigurableServletWebServerFactory casServletWebServerFactory;

    @Autowired
    @Qualifier("casTomcatEmbeddedServletContainerCustomizer")
    private ServletWebServerFactoryCustomizer casTomcatEmbeddedServletContainerCustomizer;

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void registerMulticastProperties(final DynamicPropertyRegistry registry) throws SocketException {
        val bind = pickMulticastBindAddress();
        registry.add(
            "cas.server.tomcat.clustering.membership-bind-address",
            () -> bind
        );
    }
    
    @Test
    void verifyOperation() throws Throwable {
        casTomcatEmbeddedServletContainerCustomizer.customize(casServletWebServerFactory);
        val server = casServletWebServerFactory.getWebServer();
        try {
            val tomcatServer = (TomcatWebServer) server;
            tomcatServer.start();
            val cluster = (SimpleTcpCluster) tomcatServer.getTomcat().getEngine().getCluster();
            val channel = (GroupChannel) cluster.getChannel();
            val membership = channel.getMembershipService();
            assertInstanceOf(McastService.class, membership);

            val givenRemoteUser = UUID.randomUUID().toString();
            val response = HttpUtils.execute(HttpExecutionRequest.builder()
                .method(HttpMethod.GET)
                .headers(Map.of("REMOTE_USER", givenRemoteUser))
                .url("http://localhost:%s/custom".formatted(port))
                .build());
            val responseHeader = response.getHeader("X-Remote-User");
            assertNotNull(responseHeader);
            val remoteUser = responseHeader.getValue();
            assertEquals(givenRemoteUser, remoteUser);
        } finally {
            server.stop();
        }
    }

    private static String pickMulticastBindAddress() throws SocketException {
        for (val nif : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (nif.isUp() && !nif.isLoopback() && nif.supportsMulticast()) {
                for (val addr : Collections.list(nif.getInetAddresses())) {
                    if (addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        }
        return "127.0.0.1";
    }

}

