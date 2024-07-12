package org.apereo.cas.adaptors.radius.authentication;

import org.apereo.cas.adaptors.radius.web.flow.BaseRadiusMultifactorAuthenticationTests;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import net.jradius.dictionary.Attr_State;
import net.jradius.packet.attribute.value.StringValue;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import javax.security.auth.login.FailedLoginException;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RadiusTokenAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseRadiusMultifactorAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.radius.server.protocol=PAP",
        "cas.authn.mfa.radius.client.shared-secret=testing123",
        "cas.authn.mfa.radius.client.inet-address=localhost"
    })
@Tag("Radius")
@ExtendWith(CasTestExtension.class)
@EnabledOnOs(OS.LINUX)
class RadiusTokenAuthenticationHandlerTests {

    @Autowired
    @Qualifier("radiusTokenAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Throwable {
        val credential = new RadiusTokenCredential("Mellon");

        assertTrue(authenticationHandler.supports(credential));
        assertTrue(authenticationHandler.supports(credential.getClass()));

        val context = MockRequestContext.create(applicationContext);

        assertThrows(FailedLoginException.class, () -> authenticationHandler.authenticate(credential, mock(Service.class)));

        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser", 
            Map.of(Attr_State.NAME, List.of(new StringValue("value"))));
        val authn = CoreAuthenticationTestUtils.getAuthentication(principal);
        WebUtils.putAuthentication(authn, context);
        val result = authenticationHandler.authenticate(credential, mock(Service.class));
        assertNotNull(result);
    }
}
