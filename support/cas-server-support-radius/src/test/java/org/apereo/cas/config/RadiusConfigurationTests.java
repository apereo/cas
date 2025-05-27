package org.apereo.cas.config;

import org.apereo.cas.adaptors.radius.RadiusServer;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.radius.RadiusClientProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import net.jradius.dictionary.Attr_ReplyMessage;
import net.jradius.dictionary.Attr_State;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.DefaultTransitionCriteria;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Jozef Kotlar
 * @since 5.3.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreWebflowAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasRadiusAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class
},
    properties = {
        "cas.authn.radius.client.shared-secret=NoSecret",
        "cas.authn.radius.client.inet-address=localhost,localguest",
        "cas.authn.mfa.radius.id=" + TestMultifactorAuthenticationProvider.ID
    })
@Tag("Radius")
@ExtendWith(CasTestExtension.class)
class RadiusConfigurationTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("radiusAccessChallengedAuthenticationWebflowEventResolver")
    private ObjectProvider<CasWebflowEventResolver> radiusAccessChallengedAuthenticationWebflowEventResolver;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("radiusServers")
    private BeanContainer<RadiusServer> radiusServers;

    @Autowired
    @Qualifier("radiusServer")
    private RadiusServer radiusServer;

    @Test
    void emptyAddress() {
        val clientProperties = new RadiusClientProperties();
        clientProperties.setInetAddress("  ");
        val ips = CasRadiusAutoConfiguration.getClientIps(clientProperties);
        assertEquals(0, ips.size());
    }

    @Test
    void someAddressesWithSpaces() {
        val clientProperties = new RadiusClientProperties();
        clientProperties.setInetAddress("localhost,  localguest  ");
        val ips = CasRadiusAutoConfiguration.getClientIps(clientProperties);
        assertEquals(2, ips.size());
        assertTrue(ips.contains("localhost"));
        assertTrue(ips.contains("localguest"));
    }

    @Test
    void radiusServer() {
        assertNotNull(this.radiusServer);
    }

    @Test
    void radiusServers() {
        assertEquals("localhost,localguest", casProperties.getAuthn().getRadius().getClient().getInetAddress());
        assertNotNull(radiusServers);
        assertEquals(2, radiusServers.size());
    }

    @Test
    void verifyAccessChallengedWebflowEventResolver() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        var result = radiusAccessChallengedAuthenticationWebflowEventResolver.getObject().resolve(context);
        assertNull(result);

        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            CollectionUtils.wrap(Attr_ReplyMessage.NAME, "Reply-Back", Attr_State.NAME, "State".getBytes(StandardCharsets.UTF_8)));

        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(principal), context);
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

        val targetResolver = new DefaultTargetStateResolver(TestMultifactorAuthenticationProvider.ID);
        val transition = new Transition(new DefaultTransitionCriteria(
            new LiteralExpression(TestMultifactorAuthenticationProvider.ID)), targetResolver);
        context.getRootFlow().getGlobalTransitionSet().add(transition);

        result = radiusAccessChallengedAuthenticationWebflowEventResolver.getObject().resolve(context);
        assertEquals(1, result.size());
        assertEquals(TestMultifactorAuthenticationProvider.ID, result.iterator().next().getId());
    }
}
