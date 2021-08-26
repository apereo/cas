package org.apereo.cas.web.flow;

import org.apereo.cas.BaseRemoteAddressTests;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.message.DefaultMessageContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RemoteAddressNonInteractiveCredentialsActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = BaseRemoteAddressTests.SharedTestConfiguration.class,
    properties = "cas.authn.remote-address.ip-address-range=192.168.1.0/255.255.255.0")
@Tag("WebflowActions")
public class RemoteAddressNonInteractiveCredentialsActionTests {

    @Autowired
    @Qualifier("remoteAddressCheck")
    private Action remoteAddressCheck;

    @Test
    public void verifyOperation() throws Exception {
        val context = new MockRequestContext();
        val messageContext = (DefaultMessageContext) context.getMessageContext();
        messageContext.setMessageSource(mock(MessageSource.class));

        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        assertNotNull(remoteAddressCheck.execute(context));
    }

    @Test
    public void verifyFails() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.setRemoteAddr(StringUtils.EMPTY);
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, remoteAddressCheck.execute(context).getId());
    }
}
