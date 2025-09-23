package org.apereo.cas.syncope.authentication;

import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.syncope.BaseSyncopeTests;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TenantSyncopeAuthenticationHandlerBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Syncope")
@ExtendWith(CasTestExtension.class)
@EnabledIfListeningOnPort(port = 18080)
@SpringBootTest(
    classes = BaseSyncopeTests.SharedTestConfiguration.class,
    properties = {
        "cas.multitenancy.core.enabled=true",
        "cas.multitenancy.json.location=classpath:/tenants.json"
    })
class TenantSyncopeAuthenticationHandlerBuilderTests extends BaseSyncopeTests {
    @Autowired
    @Qualifier(AuthenticationManager.BEAN_NAME)
    private AuthenticationManager authenticationManager;

    @Test
    void verifyHandlerPasses() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        request.setContextPath("/tenants/shire/login");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
        
        val credential = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("syncopecas", "Mellon");
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
            .newTransaction(CoreAuthenticationTestUtils.getService(), credential);
        val result = authenticationManager.authenticate(transaction);
        assertNotNull(result);
    }

}
