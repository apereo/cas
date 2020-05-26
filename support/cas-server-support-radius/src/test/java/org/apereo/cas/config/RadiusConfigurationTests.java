package org.apereo.cas.config;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.radius.RadiusClientProperties;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import net.jradius.dictionary.Attr_ReplyMessage;
import net.jradius.dictionary.Attr_State;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.DefaultTransitionCriteria;
import org.springframework.webflow.test.MockRequestContext;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jozef Kotlar
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    RadiusConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCookieConfiguration.class,
    RefreshAutoConfiguration.class
},
    properties = {
        "cas.authn.radius.client.sharedSecret=NoSecret",
        "cas.authn.radius.client.inet-address=localhost,localguest",
        "cas.authn.mfa.radius.id=" + TestMultifactorAuthenticationProvider.ID
    })
@Tag("Radius")
@EnabledIfPortOpen(port = 1812)
public class RadiusConfigurationTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("radiusConfiguration")
    private ObjectProvider<RadiusConfiguration> radiusConfiguration;

    @Autowired
    @Qualifier("radiusAccessChallengedAuthenticationWebflowEventResolver")
    private ObjectProvider<CasWebflowEventResolver> radiusAccessChallengedAuthenticationWebflowEventResolver;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

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
        assertNotNull(radiusConfiguration.getObject().radiusServer());
    }

    @Test
    public void radiusServers() {
        assertEquals("localhost,localguest", casProperties.getAuthn().getRadius().getClient().getInetAddress());
        val servers = radiusConfiguration.getObject().radiusServers();
        assertNotNull(servers);
        assertEquals(2, servers.size());
    }

    @Test
    public void verifyAccessChallengedWebflowEventResolver() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        var result = radiusAccessChallengedAuthenticationWebflowEventResolver.getObject().resolve(context);
        assertNull(result);

        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            CollectionUtils.wrap(Attr_ReplyMessage.NAME, "Reply-Back", Attr_State.NAME, "State".getBytes(StandardCharsets.UTF_8)));

        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(principal), context);
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

        val targetResolver = new DefaultTargetStateResolver(TestMultifactorAuthenticationProvider.ID);
        val transition = new Transition(new DefaultTransitionCriteria(new LiteralExpression(TestMultifactorAuthenticationProvider.ID)), targetResolver);
        context.getRootFlow().getGlobalTransitionSet().add(transition);

        result = radiusAccessChallengedAuthenticationWebflowEventResolver.getObject().resolve(context);
        assertEquals(1, result.size());
        assertEquals(TestMultifactorAuthenticationProvider.ID, result.iterator().next().getId());
    }
}
