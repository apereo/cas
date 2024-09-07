package org.apereo.cas.nativex;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.ticket.registry.CachedTicketExpirationPolicy;
import org.apereo.cas.ticket.registry.RedisTicketDocument;
import org.apereo.cas.ticket.registry.pub.RedisMessagePayload;
import org.apereo.cas.ticket.registry.sub.RedisTicketRegistryMessageListener;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;

/**
 * This is {@link RedisTicketRegistryRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class RedisTicketRegistryRuntimeHints implements CasRuntimeHintsRegistrar {

    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerSerializationHints(hints, RedisMessagePayload.RedisMessageTypes.class,
            PublisherIdentifier.class,
            RedisTicketDocument.class,
            RedisMessagePayload.class);

        registerReflectionHints(hints, List.of(
                RedisMessagePayload.class,
                RedisMessagePayload.RedisMessagePayloadBuilder.class,
                CachedTicketExpirationPolicy.class,
                RedisTicketDocument.class,
                RedisTicketDocument.RedisTicketDocumentBuilder.class
            )
        );

        registerReflectionHints(hints,
            findSubclassesInPackage(RedisTicketRegistryMessageListener.class, CentralAuthenticationService.NAMESPACE));
    }
}
