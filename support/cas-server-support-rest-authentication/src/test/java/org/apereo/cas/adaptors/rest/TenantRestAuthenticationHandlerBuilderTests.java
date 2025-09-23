package org.apereo.cas.adaptors.rest;

import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockWebServer;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TenantRestAuthenticationHandlerBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@SpringBootTest(
    classes = BaseRestAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.multitenancy.core.enabled=true",
        "cas.multitenancy.json.location=classpath:/tenants.json"
    })
@Tag("RestfulApiAuthentication")
@ExtendWith(CasTestExtension.class)
class TenantRestAuthenticationHandlerBuilderTests {
    @Autowired
    @Qualifier(AuthenticationManager.BEAN_NAME)
    private AuthenticationManager authenticationManager;

    @Test
    void verifyOperation() throws Throwable {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        request.setContextPath("/tenants/shire/login");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));

        val credential = CoreAuthenticationTestUtils.getCredentialsWithDifferentUsernameAndPassword("casuser", "Mellon");
        
        val transaction = CoreAuthenticationTestUtils.getAuthenticationTransactionFactory()
            .newTransaction(RegisteredServiceTestUtils.getService(), credential);

        try (val webServer = new MockWebServer(19999)) {
            webServer.start();
            webServer.responseBodyJson(PrincipalFactoryUtils.newPrincipalFactory().createPrincipal("casuser"));
            webServer.responseStatus(HttpStatus.OK);

            val result = authenticationManager.authenticate(transaction);
            assertNotNull(result);
            assertEquals("casuser", result.getPrincipal().getId());
        }

    }
}
