package org.jasig.cas.support.oauth.web;

import static org.junit.Assert.*;

import org.jasig.cas.support.oauth.OAuthConstants;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * This class tests the {@link OAuth20WrapperController} class.
 *
 * @author Jerome Leleu
 * @since 3.5.2
 */
public class OAuth20WrapperControllerTests {

    private static final String CONTEXT = "/oauth2.0/";

    @Test
    public void verifyWrongMethod() throws Exception {
        final MockHttpServletRequest mockRequest = new MockHttpServletRequest("GET", CONTEXT + "wrongmethod");
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final OAuth20WrapperController oauth20WrapperController = new OAuth20WrapperController();
        oauth20WrapperController.handleRequest(mockRequest, mockResponse);
        assertEquals(200, mockResponse.getStatus());
        assertEquals("error=" + OAuthConstants.INVALID_REQUEST, mockResponse.getContentAsString());
    }
}
