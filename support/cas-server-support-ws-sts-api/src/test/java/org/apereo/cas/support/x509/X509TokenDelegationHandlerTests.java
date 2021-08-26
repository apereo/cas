package org.apereo.cas.support.x509;

import lombok.val;
import org.apache.cxf.sts.request.ReceivedToken;
import org.apache.cxf.sts.token.delegation.TokenDelegationParameters;
import org.apache.wss4j.dom.WSConstants;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link X509TokenDelegationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WSFederation")
public class X509TokenDelegationHandlerTests {

    @Test
    public void verifyHandle() {
        val handler = new X509TokenDelegationHandler();

        val elementResponse = mock(Element.class);
        when(elementResponse.getLocalName()).thenReturn(WSConstants.X509_DATA_LN);
        when(elementResponse.getNamespaceURI()).thenReturn(WSConstants.SIG_NS);

        val token = mock(ReceivedToken.class);
        when(token.getToken()).thenReturn(elementResponse);
        assertTrue(handler.canHandleToken(token));
    }

    @Test
    public void verifyDelegation() {
        val params = new TokenDelegationParameters();

        val handler = new X509TokenDelegationHandler();

        val elementResponse = mock(Element.class);
        when(elementResponse.getLocalName()).thenReturn(WSConstants.X509_DATA_LN);

        val token = mock(ReceivedToken.class);
        when(token.getToken()).thenReturn(elementResponse);
        when(token.getState()).thenReturn(ReceivedToken.STATE.VALID);
        when(token.getPrincipal()).thenReturn(mock(Principal.class));
        when(token.isDOMElement()).thenReturn(Boolean.TRUE);

        params.setToken(token);
        val response = handler.isDelegationAllowed(params);
        assertTrue(response.isDelegationAllowed());
    }

}
