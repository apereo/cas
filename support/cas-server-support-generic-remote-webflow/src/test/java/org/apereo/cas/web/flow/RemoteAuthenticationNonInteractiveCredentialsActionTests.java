package org.apereo.cas.web.flow;

import org.apereo.cas.BaseRemoteAddressTests;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import jakarta.servlet.http.Cookie;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RemoteAuthenticationNonInteractiveCredentialsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */

@Tag("WebflowActions")
class RemoteAuthenticationNonInteractiveCredentialsActionTests {
    @Nested
    @SpringBootTest(classes = BaseRemoteAddressTests.SharedTestConfiguration.class,
        properties = "cas.authn.remote.ip-address-range=192.168.1.0/255.255.255.0")
    class RemoteAddressTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_REMOTE_AUTHENTICATION_ADDRESS_CHECK)
        private Action remoteAuthenticationCheck;

        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Test
        void verifyOperation() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            assertNotNull(remoteAuthenticationCheck.execute(context));
        }

        @Test
        void verifyFails() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setRemoteAddr(StringUtils.EMPTY);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, remoteAuthenticationCheck.execute(context).getId());
        }
    }

    @Nested
    @SpringBootTest(classes = BaseRemoteAddressTests.SharedTestConfiguration.class,
        properties = {
            "cas.authn.remote.cookie.cookie-name=MyRemoteCookie",
            "cas.authn.remote.cookie.crypto.encryption.key=u696jJnPvm1DHLR7yVCSKMMzzoPoFxJZW4-MP1CkM5w",
            "cas.authn.remote.cookie.crypto.signing.key=zPdNCd0R1oMR0ClzEqZzapkte8rO0tNvygYjmHoUhitAu6CBscwMC3ZTKy8tleTKiQ6GVcuiQQgxfd1nSKxf7w"
        })
    class RemoteCookieTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_REMOTE_AUTHENTICATION_ADDRESS_CHECK)
        private Action remoteAuthenticationCheck;

        @Autowired
        private ConfigurableApplicationContext applicationContext;

        @Test
        void verifyOperation() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.getHttpServletRequest().setCookies(new Cookie("MyRemoteCookie", "1234567890"));
            assertNotNull(remoteAuthenticationCheck.execute(context));
        }
    }
}
