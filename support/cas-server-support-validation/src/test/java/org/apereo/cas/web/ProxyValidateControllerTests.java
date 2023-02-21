package org.apereo.cas.web;

import org.apereo.cas.BaseCasCoreTests;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasThymeleafConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.web.config.CasThemesConfiguration;
import org.apereo.cas.web.config.CasValidationConfiguration;
import org.apereo.cas.web.v2.ProxyValidateController;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ProxyValidateControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    BaseCasCoreTests.SharedTestConfiguration.class,
    CasThemesConfiguration.class,
    CasThymeleafConfiguration.class,
    CasValidationConfiguration.class
})
@Tag("CAS")
@Getter
public class ProxyValidateControllerTests {
    protected static final String SUCCESS = "Success";

    protected static final Service SERVICE = RegisteredServiceTestUtils.getService("https://www.casinthecloud.com");

    @Autowired
    @Qualifier(AuthenticationSystemSupport.BEAN_NAME)
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier(CentralAuthenticationService.BEAN_NAME)
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("proxyValidateController")
    private ProxyValidateController proxyValidateController;

    @Test
    public void verifyValidServiceTicket() throws Exception {
        val ctx = CoreAuthenticationTestUtils.getAuthenticationResult(getAuthenticationSystemSupport(), SERVICE);

        val tId = getCentralAuthenticationService().createTicketGrantingTicket(ctx);
        val sId = getCentralAuthenticationService().grantServiceTicket(tId.getId(), SERVICE, ctx);

        val request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_SERVICE, SERVICE.getId());
        request.addParameter(CasProtocolConstants.PARAMETER_TICKET, sId.getId());

        val mv = proxyValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertTrue(Objects.requireNonNull(mv.getView()).toString().contains(SUCCESS));
    }
}
