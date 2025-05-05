package org.apereo.cas.adaptors.radius.web.flow;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasRadiusAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import net.jradius.dictionary.Attr_ReplyMessage;
import net.jradius.dictionary.Attr_State;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
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
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasRadiusAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreWebflowAutoConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class,
    CasCoreAutoConfiguration.class
}, properties = {
    "cas.authn.radius.server.protocol=PAP",
    "cas.authn.radius.client.shared-secret=testing123",
    "cas.authn.radius.client.inet-address=localhost",
    "cas.authn.mfa.radius.id=mfa-dummy"
})
@Tag("Radius")
@ExtendWith(CasTestExtension.class)
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
