package org.apereo.cas.web.flow;

import org.apereo.cas.BaseRemoteAddressTests;
import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RemoteAddressNonInteractiveCredentialsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseRemoteAddressTests.SharedTestConfiguration.class,
    properties = "cas.authn.remote-address.ip-address-range=192.168.1.0/255.255.255.0")
@Tag("WebflowActions")
class RemoteAddressNonInteractiveCredentialsActionTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_REMOTE_AUTHENTICATION_ADDRESS_CHECK)
    private Action remoteAddressCheck;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create();
        assertNotNull(remoteAddressCheck.execute(context));
    }

    @Test
    void verifyFails() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.getHttpServletRequest().setRemoteAddr(StringUtils.EMPTY);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, remoteAddressCheck.execute(context).getId());
    }
}
