package org.apereo.cas.adaptors.radius.web.flow;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.config.CasCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasWebflowAutoConfiguration;
import org.apereo.cas.config.RadiusConfiguration;
import org.apereo.cas.util.CollectionUtils;
import lombok.val;
import net.jradius.dictionary.Attr_ReplyMessage;
import net.jradius.dictionary.Attr_State;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RadiusAccessChallengedMultifactorAuthenticationTriggerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RadiusConfiguration.class,
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasWebflowAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCookieAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreAutoConfiguration.class
}, properties = {
    "cas.authn.radius.server.protocol=PAP",
    "cas.authn.radius.client.shared-secret=testing123",
    "cas.authn.radius.client.inet-address=localhost",
    "cas.authn.mfa.radius.id=mfa-dummy"
})
@Tag("Radius")
class RadiusAccessChallengedMultifactorAuthenticationTriggerTests {
    @Autowired
    @Qualifier("radiusAccessChallengedMultifactorAuthenticationTrigger")
    private MultifactorAuthenticationTrigger multifactorAuthenticationTrigger;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyTriggerInactive() throws Throwable {
        assertTrue(multifactorAuthenticationTrigger.isActivated(CoreAuthenticationTestUtils.getAuthentication(),
            CoreAuthenticationTestUtils.getRegisteredService(), new MockHttpServletRequest(),
            new MockHttpServletResponse(),
            CoreAuthenticationTestUtils.getService()).isEmpty());
        assertTrue(multifactorAuthenticationTrigger.isActivated(null,
            CoreAuthenticationTestUtils.getRegisteredService(), new MockHttpServletRequest(),
            new MockHttpServletResponse(),
            CoreAuthenticationTestUtils.getService()).isEmpty());
    }

    @Test
    void verifyTriggerActive() throws Throwable {
        val authn = CoreAuthenticationTestUtils.getAuthentication(CoreAuthenticationTestUtils.getPrincipal(
            CollectionUtils.wrap(Attr_ReplyMessage.NAME, "reply-message", Attr_State.NAME, "whatever")
        ));

        assertThrows(AuthenticationException.class, () -> multifactorAuthenticationTrigger.isActivated(authn,
            CoreAuthenticationTestUtils.getRegisteredService(), new MockHttpServletRequest(),
            new MockHttpServletResponse(),
            CoreAuthenticationTestUtils.getService()));

        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val authnMfa = CoreAuthenticationTestUtils.getAuthentication(CoreAuthenticationTestUtils.getPrincipal(
            CollectionUtils.wrap(Attr_ReplyMessage.NAME, "reply-message", Attr_State.NAME, "whatever")
        ));

        assertTrue(multifactorAuthenticationTrigger.isActivated(authnMfa,
            CoreAuthenticationTestUtils.getRegisteredService(), new MockHttpServletRequest(),
            new MockHttpServletResponse(),
            CoreAuthenticationTestUtils.getService()).isPresent());
    }
}
