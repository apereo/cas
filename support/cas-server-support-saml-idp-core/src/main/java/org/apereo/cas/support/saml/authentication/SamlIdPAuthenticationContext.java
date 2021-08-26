package org.apereo.cas.support.saml.authentication;

import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.serialization.SerializationUtils;

import lombok.val;
import org.opensaml.messaging.context.BaseContext;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.messaging.context.SAMLBindingContext;
import org.opensaml.saml.common.messaging.context.SAMLPeerEntityContext;
import org.opensaml.saml.common.messaging.context.SAMLProtocolContext;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This is {@link SamlIdPAuthenticationContext}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class SamlIdPAuthenticationContext implements Serializable {
    private static final long serialVersionUID = 696048495600124624L;

    private final Map<String, SamlIdPAuthenticationContext> contexts = new LinkedHashMap<>();

    private final Map<String, Serializable> properties = new LinkedHashMap<>();

    /**
     * From.
     *
     * @param context the context
     * @return the saml id p authentication context
     */
    public static SamlIdPAuthenticationContext from(final MessageContext context) {
        val result = new SamlIdPAuthenticationContext();
        if (context.containsSubcontext(SAMLBindingContext.class)) {
            val binding = Objects.requireNonNull(context.getSubcontext(SAMLBindingContext.class));
            result.getSubcontext(SAMLBindingContext.class).put("relayState", binding.getRelayState());
            result.getSubcontext(SAMLBindingContext.class).put("hasBindingSignature", binding.hasBindingSignature());
        }
        if (context.containsSubcontext(SAMLProtocolContext.class)) {
            val protocol = Objects.requireNonNull(context.getSubcontext(SAMLProtocolContext.class));
            result.getSubcontext(SAMLProtocolContext.class).put("protocol", protocol.getProtocol());
        }
        if (context.containsSubcontext(SAMLPeerEntityContext.class)) {
            val peer = Objects.requireNonNull(context.getSubcontext(SAMLPeerEntityContext.class));
            result.getSubcontext(SAMLPeerEntityContext.class).put("entityId", peer.getEntityId());
            result.getSubcontext(SAMLPeerEntityContext.class).put("authenticated", peer.isAuthenticated());
        }
        return result;
    }

    /**
     * Decode.
     *
     * @param data the data
     * @return the saml id p authentication context
     */
    public static SamlIdPAuthenticationContext decode(final String data) {
        val decoded = EncodingUtils.decodeBase64(data);
        return SerializationUtils.deserialize(decoded, SamlIdPAuthenticationContext.class);
    }

    /**
     * Encode.
     *
     * @return the string
     */
    public String encode() {
        return EncodingUtils.encodeBase64(SerializationUtils.serialize(this));
    }

    /**
     * To message context.
     *
     * @param message the message
     * @return the message context
     */
    public MessageContext toMessageContext(final Object message) {
        val messageContext = new MessageContext();
        messageContext.setMessage(message);
        if (contexts.containsKey(SAMLBindingContext.class.getName())) {
            val binding = contexts.get(SAMLBindingContext.class.getName());
            val subcontext = messageContext.getSubcontext(SAMLBindingContext.class, true);
            subcontext.setHasBindingSignature((boolean) binding.properties.get("hasBindingSignature"));
            subcontext.setRelayState((String) binding.properties.get("relayState"));
        }
        if (contexts.containsKey(SAMLProtocolContext.class.getName())) {
            val binding = contexts.get(SAMLProtocolContext.class.getName());
            val subcontext = messageContext.getSubcontext(SAMLProtocolContext.class, true);
            subcontext.setProtocol((String) binding.properties.get("protocol"));
        }
        if (contexts.containsKey(SAMLPeerEntityContext.class.getName())) {
            val binding = contexts.get(SAMLPeerEntityContext.class.getName());
            val subcontext = messageContext.getSubcontext(SAMLPeerEntityContext.class, true);
            subcontext.setEntityId((String) binding.properties.get("entityId"));
        }
        return messageContext;
    }

    /**
     * Gets subcontext.
     *
     * @param clazz the clazz
     * @return the subcontext
     */
    public SamlIdPAuthenticationContext getSubcontext(final Class<? extends BaseContext> clazz) {
        if (!contexts.containsKey(clazz.getName())) {
            contexts.put(clazz.getName(), new SamlIdPAuthenticationContext());
        }
        return contexts.get(clazz.getName());
    }

    /**
     * Put context property.
     *
     * @param name  the name
     * @param value the value
     */
    public void put(final String name, final Serializable value) {
        if (value != null) {
            properties.put(name, value);
        }
    }
}
