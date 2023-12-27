package org.apereo.cas.nativex;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.ticket.registry.CachedTicketExpirationPolicy;
import org.apereo.cas.ticket.registry.RedisCompositeKey;
import org.apereo.cas.ticket.registry.RedisTicketDocument;
import org.apereo.cas.ticket.registry.pub.RedisMessagePayload;
import org.apereo.cas.ticket.registry.sub.RedisTicketRegistryMessageListener;
import org.apereo.cas.util.PublisherIdentifier;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;
import java.util.Collection;
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
                RedisCompositeKey.class,
                RedisTicketDocument.class,
                RedisTicketDocument.RedisTicketDocumentBuilder.class
            )
        );

        registerReflectionHints(hints,
            findSubclassesInPackage(RedisTicketRegistryMessageListener.class, CentralAuthenticationService.NAMESPACE));
    }

    private static void registerReflectionHints(final RuntimeHints hints, final Collection entries) {
        val memberCategories = new MemberCategory[]{
            MemberCategory.INTROSPECT_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.PUBLIC_FIELDS};
        entries.forEach(el -> {
            if (el instanceof final Class clazz) {
                hints.reflection().registerType(clazz, memberCategories);
            }
            if (el instanceof final TypeReference reference) {
                hints.reflection().registerType(reference, memberCategories);
            }
        });
    }
}
