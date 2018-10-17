package org.apereo.cas.config;

import org.apereo.cas.category.RadiusCategory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.radius.RadiusClientProperties;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;

/**
 * @author Jozef Kotlar
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasMultifactorAuthenticationWebflowConfiguration.class,
    RadiusConfiguration.class,
    RefreshAutoConfiguration.class
})
@TestPropertySource(properties = {
    "cas.authn.radius.client.sharedSecret=NoSecret",
    "cas.authn.radius.client.inetAddress=localhost,localguest"
})
@Category(RadiusCategory.class)
public class RadiusConfigurationTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("radiusConfiguration")
    private RadiusConfiguration radiusConfiguration;

    @Test
    public void emptyAddress() {
        val clientProperties = new RadiusClientProperties();
        clientProperties.setInetAddress("  ");
        val ips = RadiusConfiguration.getClientIps(clientProperties);
        assertEquals(0, ips.size());
    }

    @Test
    public void someAddressesWithSpaces() {
        val clientProperties = new RadiusClientProperties();
        clientProperties.setInetAddress("localhost,  localguest  ");
        val ips = RadiusConfiguration.getClientIps(clientProperties);
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
        val servers = radiusConfiguration.radiusServers();
        assertNotNull(servers);
        assertEquals(2, servers.size());
    }
}
