package org.apereo.cas.support.saml.authentication;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.messaging.context.SAMLProtocolContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPAuthenticationContextTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SAML")
public class SamlIdPAuthenticationContextTests {
    @Test
    public void verifyOperation() {
        val messageContext = new MessageContext();
        messageContext.getSubcontext(SAMLBindingContext.class, true).setRelayState(UUID.randomUUID().toString());
        messageContext.getSubcontext(SAMLBindingContext.class, true).setHasBindingSignature(true);

        messageContext.getSubcontext(SAMLProtocolContext.class, true).setProtocol(UUID.randomUUID().toString());
        messageContext.getSubcontext(SAMLPeerEntityContext.class, true).setEntityId(UUID.randomUUID().toString());

        val ctx = SamlIdPAuthenticationContext.from(messageContext);
        val encoded = ctx.encode();
        assertNotNull(encoded);

        val result = SamlIdPAuthenticationContext.decode(encoded).toMessageContext(new Object());
        assertNotNull(result);
        assertNotNull(result.getMessage());
    }
}
