package org.apereo.cas.tomcat;

import org.apereo.cas.config.CasEmbeddedContainerTomcatAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import lombok.val;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CasTomcatServletWebServerFactoryCloudClusterTests}.
 *
 * @author Hal Deadman
 * @since 6.2.0
 */
@SpringBootTest(classes = CasEmbeddedContainerTomcatAutoConfiguration.class, properties = {
    "server.port=8183",
    "server.ssl.enabled=false"
}, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@EnableConfigurationProperties({CasConfigurationProperties.class, ServerProperties.class})
@Tag("WebApp")
class CasTomcatServletWebServerFactoryCloudClusterTests {
    @Autowired
    protected CasConfigurationProperties casProperties;

    @Autowired
    protected ServerProperties serverProperties;

    @Autowired
    @Qualifier("casServletWebServerFactory")
    private ConfigurableServletWebServerFactory casServletWebServerFactory;

    @Autowired
    @Qualifier("casTomcatEmbeddedServletContainerCustomizer")
    private ServletWebServerFactoryCustomizer casTomcatEmbeddedServletContainerCustomizer;

    @Test
    void verifyOperation() throws Throwable {
        casTomcatEmbeddedServletContainerCustomizer.customize(casServletWebServerFactory);
        val server = casServletWebServerFactory.getWebServer();
        assertNotNull(server);
    }


    @Test
    void verifyDynamicCloud() throws Throwable {
        val props = new CasConfigurationProperties();
        props.getServer().getTomcat().getClustering()
            .setEnabled(true).setClusteringType("CLOUD");

        val factory = new CasTomcatServletWebServerFactory(props, serverProperties);

        val tomcat = mock(Tomcat.class);
        when(tomcat.getEngine()).thenReturn(mock(Engine.class));
        val service = mock(Service.class);
        when(service.findConnectors()).thenReturn(new Connector[]{});

        val server = mock(Server.class);
        when(server.findServices()).thenReturn(new Service[]{service});

        val host = mock(Host.class);
        val context = mock(Context.class);
        when(context.getBaseName()).thenReturn("cas");
        when(context.getState()).thenReturn(LifecycleState.STARTED);
        when(host.findChildren()).thenReturn(new Container[]{context});

        when(tomcat.getHost()).thenReturn(host);
        when(tomcat.getService()).thenReturn(service);
        when(tomcat.getServer()).thenReturn(server);

        assertDoesNotThrow(() -> {
            factory.getTomcatWebServer(tomcat);
        });
    }

    @Test
    void verifyStaticCloud() throws Throwable {
        val props = new CasConfigurationProperties();
        props.getServer().getTomcat().getClustering()
            .setEnabled(true)
            .setClusteringType("STATIC")
            .setClusterMembers("127.0.0.1:1234:0,127.0.0.2:1235:0")
            .setManagerType("DEFAULT");
        val factory = new CasTomcatServletWebServerFactory(props, serverProperties);
        val tc = mock(Tomcat.class);
        when(tc.getEngine()).thenReturn(mock(Engine.class));
        val service = mock(Service.class);
        when(service.findConnectors()).thenReturn(new Connector[]{});

        val server = mock(Server.class);
        when(server.findServices()).thenReturn(new Service[]{service});

        val host = mock(Host.class);
        val context = mock(Context.class);
        when(context.getBaseName()).thenReturn("cas");
        when(context.getState()).thenReturn(LifecycleState.STARTED);
        when(host.findChildren()).thenReturn(new Container[]{context});

        when(tc.getHost()).thenReturn(host);
        when(tc.getService()).thenReturn(service);
        when(tc.getServer()).thenReturn(server);

        assertDoesNotThrow(() -> {
            factory.getTomcatWebServer(tc);
        });
    }
}
