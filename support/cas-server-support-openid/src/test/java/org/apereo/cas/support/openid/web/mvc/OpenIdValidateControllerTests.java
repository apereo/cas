package org.apereo.cas.support.openid.web.mvc;

import org.apereo.cas.support.openid.AbstractOpenIdTests;
import org.apereo.cas.support.openid.OpenIdProtocolConstants;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openid4java.message.VerifyResponse;
import org.openid4java.server.ServerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link OpenIdValidateControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 * @deprecated 6.2
 */
@Deprecated(since = "6.3.0")
@Tag("Simple")
@Import(OpenIdValidateControllerTests.OpenIdValidateControllerTestConfiguration.class)
public class OpenIdValidateControllerTests extends AbstractOpenIdTests {
    private final MockHttpServletRequest request = new MockHttpServletRequest();

    private final HttpServletResponse response = new MockHttpServletResponse();

    @Autowired
    @Qualifier("casOpenIdServiceSuccessView")
    private View casOpenIdServiceSuccessView;

    @Autowired
    @Qualifier("casOpenIdServiceFailureView")
    private View casOpenIdServiceFailureView;

    @Autowired
    @Qualifier("openIdValidateController")
    private OpenIdValidateController controller;

    @Test
    public void verifySuccess() throws Exception {
        assertFalse(controller.canHandle(request, response));
        request.addParameter(OpenIdProtocolConstants.OPENID_MODE, OpenIdProtocolConstants.CHECK_AUTHENTICATION);
        assertTrue(controller.canHandle(request, response));

        request.setParameter("pass", "true");
        var mv = controller.handleRequestInternal(request, response);
        assertEquals(casOpenIdServiceSuccessView.toString(), mv.getView().toString());

        request.setParameter("pass", "false");
        mv = controller.handleRequestInternal(request, response);
        assertEquals(casOpenIdServiceFailureView.toString(), mv.getView().toString());
    }

    @TestConfiguration("OpenIdValidateControllerTestConfiguration")
    public static class OpenIdValidateControllerTestConfiguration {
        @Bean
        public ServerManager serverManager() {
            val mock = mock(ServerManager.class);
            val message = mock(VerifyResponse.class);
            when(message.isSignatureVerified()).thenReturn(Boolean.TRUE);
            when(mock.verify(argThat(arg -> arg != null && arg.getParameterValue("pass").equals("true")))).thenReturn(message);
            when(mock.verify(argThat(arg -> arg != null && arg.getParameterValue("pass").equals("false")))).thenReturn(mock(VerifyResponse.class));
            return mock;
        }
    }
}
