package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.oauth.OAuth20Constants;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20DeviceUserCodeApprovalEndpointControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("OAuth")
public class OAuth20DeviceUserCodeApprovalEndpointControllerTests extends AbstractOAuth20Tests {
    @Autowired
    @Qualifier("deviceUserCodeApprovalEndpointController")
    private OAuth20DeviceUserCodeApprovalEndpointController callbackAuthorizeController;

    @BeforeEach
    public void initialize() {
        clearAllServices();
    }

    @Test
    public void verifyGet() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val mv = callbackAuthorizeController.handleGetRequest(request, response);
        assertTrue(mv.getModel().containsKey("prefix"));
        assertFalse(mv.getModel().containsKey("error"));
        assertEquals(OAuth20Constants.DEVICE_CODE_APPROVAL_VIEW, mv.getViewName());
    }

    @Test
    public void verifyPostNoCode() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        var mv = callbackAuthorizeController.handlePostRequest(request, response);
        assertTrue(mv.getModel().containsKey("error"));
        val id = UUID.randomUUID().toString();
        request.setParameter(OAuth20DeviceUserCodeApprovalEndpointController.PARAMETER_USER_CODE, id);
        mv = callbackAuthorizeController.handlePostRequest(request, response);
        assertTrue(mv.getModel().containsKey("error"));
    }

    @Test
    public void verifyApproval() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val devCode = defaultDeviceTokenFactory.createDeviceCode(RegisteredServiceTestUtils.getService());
        val uc = defaultDeviceUserCodeFactory.createDeviceUserCode(devCode);
        ticketRegistry.addTicket(uc);
        request.setParameter(OAuth20DeviceUserCodeApprovalEndpointController.PARAMETER_USER_CODE, uc.getId());
        var mv = callbackAuthorizeController.handlePostRequest(request, response);
        assertFalse(mv.getModel().containsKey("error"));
        assertTrue(uc.isUserCodeApproved());
        assertEquals(OAuth20Constants.DEVICE_CODE_APPROVED_VIEW, mv.getViewName());

        mv = callbackAuthorizeController.handlePostRequest(request, response);
        assertTrue(mv.getModel().containsKey("prefix"));
        assertTrue(mv.getModel().containsKey("error"));
    }
}
