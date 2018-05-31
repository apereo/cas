package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.radius.RadiusClientProperties;
import org.apereo.cas.services.ServicesManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.*;

/**
 * @author Jozef Kotlar
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes={RadiusConfiguration.class, RefreshAutoConfiguration.class})
@TestPropertySource(locations = {"classpath:/radius2.properties"})
@Slf4j
public class RadiusConfigurationTests {

    @MockBean
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("radiusConfiguration")
    private RadiusConfiguration radiusConfiguration;

    @Test
    public void emptyAddress() {
        final var clientProperties = new RadiusClientProperties();
        clientProperties.setInetAddress("  ");
        final var ips = RadiusConfiguration.getClientIps(clientProperties);
        assertEquals(0, ips.size());
    }

    @Test
    public void someAddressesWithSpaces() {
        final var clientProperties = new RadiusClientProperties();
        clientProperties.setInetAddress("localhost,  localguest  ");
        final var ips = RadiusConfiguration.getClientIps(clientProperties);
        assertEquals(2, ips.size());
        assertTrue(ips.contains("localhost"));
        assertTrue(ips.contains("localguest"));
    }

    @Test
    public void radiusServer() {
        assertNotNull(radiusConfiguration.radiusServer());
    }

    @Test
    public void radiusServers() {
        assertEquals("localhost,localguest", casProperties.getAuthn().getRadius().getClient().getInetAddress());
        final var servers = radiusConfiguration.radiusServers();
        assertNotNull(servers);
        assertEquals(2, servers.size());
    }
}
