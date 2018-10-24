package org.apereo.cas.config;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.category.RadiusCategory;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.radius.RadiusClientProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import net.jradius.dictionary.Attr_ReplyMessage;
import net.jradius.dictionary.Attr_State;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.expression.support.LiteralExpression;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.support.DefaultTargetStateResolver;
import org.springframework.webflow.engine.support.DefaultTransitionCriteria;
import org.springframework.webflow.test.MockRequestContext;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * @author Jozef Kotlar
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    CasMultifactorAuthenticationWebflowConfiguration.class,
    RadiusConfiguration.class,
    CasCoreConfiguration.class,
    CasCoreUtilConfiguration.class,
    RefreshAutoConfiguration.class
})
@TestPropertySource(properties = {
    "cas.authn.radius.client.sharedSecret=NoSecret",
    "cas.authn.radius.client.inetAddress=localhost,localguest",
    "cas.authn.mfa.radius.id=" + TestMultifactorAuthenticationProvider.ID
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

    @Autowired
    @Qualifier("radiusAccessChallengedAuthenticationWebflowEventResolver")
    private CasWebflowEventResolver radiusAccessChallengedAuthenticationWebflowEventResolver;

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
        assertNotNull(radiusConfiguration.radiusServer());
    }

    @Test
    public void radiusServers() {
        assertEquals("localhost,localguest", casProperties.getAuthn().getRadius().getClient().getInetAddress());
        val servers = radiusConfiguration.radiusServers();
        assertNotNull(servers);
        assertEquals(2, servers.size());
    }

    @Test
    public void verifyAccessChallengedWebflowEventResolver() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        var result = radiusAccessChallengedAuthenticationWebflowEventResolver.resolve(context);
        assertNull(result);

        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            CollectionUtils.wrap(Attr_ReplyMessage.NAME, "Reply-Back", Attr_State.NAME, "State".getBytes(StandardCharsets.UTF_8)));

        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(principal), context);
        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);

        val targetResolver = new DefaultTargetStateResolver(TestMultifactorAuthenticationProvider.ID);
        val transition = new Transition(new DefaultTransitionCriteria(new LiteralExpression(TestMultifactorAuthenticationProvider.ID)), targetResolver);
        context.getRootFlow().getGlobalTransitionSet().add(transition);

        result = radiusAccessChallengedAuthenticationWebflowEventResolver.resolve(context);
        assertEquals(1, result.size());
        assertEquals(TestMultifactorAuthenticationProvider.ID, result.iterator().next().getId());
    }
}
