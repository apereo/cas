package org.apereo.cas.adaptors.radius.web.flow;

import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.RadiusConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;

import lombok.val;
import net.jradius.dictionary.Attr_ReplyMessage;
import net.jradius.dictionary.Attr_State;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;

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
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    CasCoreWebConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCookieConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreConfiguration.class
}, properties = {
    "cas.authn.radius.server.protocol=PAP",
    "cas.authn.radius.client.sharedSecret=testing123",
    "cas.authn.radius.client.inet-address=localhost",
    "cas.authn.mfa.radius.id=mfa-dummy"
})
@Tag("Radius")
@EnabledIfPortOpen(port = 1812)
public class RadiusAccessChallengedMultifactorAuthenticationTriggerTests {
    @Autowired
    @Qualifier("radiusAccessChallengedMultifactorAuthenticationTrigger")
    private MultifactorAuthenticationTrigger multifactorAuthenticationTrigger;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyTriggerInactive() {
        assertTrue(multifactorAuthenticationTrigger.isActivated(CoreAuthenticationTestUtils.getAuthentication(),
            CoreAuthenticationTestUtils.getRegisteredService(), new MockHttpServletRequest(),
            CoreAuthenticationTestUtils.getService()).isEmpty());
        assertTrue(multifactorAuthenticationTrigger.isActivated(null,
            CoreAuthenticationTestUtils.getRegisteredService(), new MockHttpServletRequest(),
            CoreAuthenticationTestUtils.getService()).isEmpty());
    }

    @Test
    public void verifyTriggerActive() {
        val authn = CoreAuthenticationTestUtils.getAuthentication(CoreAuthenticationTestUtils.getPrincipal(
            CollectionUtils.wrap(Attr_ReplyMessage.NAME, "reply-message", Attr_State.NAME, "whatever")
        ));

        assertThrows(AuthenticationException.class, () -> multifactorAuthenticationTrigger.isActivated(authn,
            CoreAuthenticationTestUtils.getRegisteredService(), new MockHttpServletRequest(),
            CoreAuthenticationTestUtils.getService()));

        TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        val authnMfa = CoreAuthenticationTestUtils.getAuthentication(CoreAuthenticationTestUtils.getPrincipal(
            CollectionUtils.wrap(Attr_ReplyMessage.NAME, "reply-message", Attr_State.NAME, "whatever")
        ));

        assertTrue(multifactorAuthenticationTrigger.isActivated(authnMfa,
            CoreAuthenticationTestUtils.getRegisteredService(), new MockHttpServletRequest(),
            CoreAuthenticationTestUtils.getService()).isPresent());
    }
}
