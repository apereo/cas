package org.apereo.cas.support.saml.authentication;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.messaging.context.SAMLProtocolContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPAuthenticationContextTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SAML")
class SamlIdPAuthenticationContextTests {
    @Test
    void verifyOperation() {
        val messageContext = new MessageContext();
        messageContext.ensureSubcontext(SAMLBindingContext.class).setRelayState(UUID.randomUUID().toString());
        messageContext.ensureSubcontext(SAMLBindingContext.class).setHasBindingSignature(true);

        messageContext.ensureSubcontext(SAMLProtocolContext.class).setProtocol(UUID.randomUUID().toString());
        messageContext.ensureSubcontext(SAMLPeerEntityContext.class).setEntityId(UUID.randomUUID().toString());

        val ctx = SamlIdPAuthenticationContext.from(messageContext);
        val encoded = ctx.encode();
        assertNotNull(encoded);

        val result = SamlIdPAuthenticationContext.decode(encoded).toMessageContext(new Object());
        assertNotNull(result);
        assertNotNull(result.getMessage());
    }
}
