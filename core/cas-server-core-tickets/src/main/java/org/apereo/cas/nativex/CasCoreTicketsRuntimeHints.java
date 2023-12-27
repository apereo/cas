package org.apereo.cas.nativex;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactoryExecutionPlanConfigurer;
import org.apereo.cas.ticket.registry.TicketCompactor;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.pubsub.QueueableTicketRegistry;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlanConfigurer;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.thread.Cleanable;
import lombok.val;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import java.util.Collection;

/**
 * This is {@link CasCoreTicketsRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasCoreTicketsRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerProxyHints(hints, TicketSerializationExecutionPlanConfigurer.class, TicketFactoryExecutionPlanConfigurer.class);

        registerSpringProxy(hints, Cleanable.class, TicketRegistry.class);
        registerSpringProxy(hints, QueueableTicketRegistry.class, TicketRegistry.class);

        registerSerializationHints(hints, findSubclassesInPackage(Ticket.class, CentralAuthenticationService.NAMESPACE));
        val expirationPolicyClasses = findSubclassesInPackage(ExpirationPolicy.class, CentralAuthenticationService.NAMESPACE);
        registerSerializationHints(hints, expirationPolicyClasses);
        registerReflectionHints(hints, expirationPolicyClasses);

        val ticketCompactors = findSubclassesInPackage(TicketCompactor.class, CentralAuthenticationService.NAMESPACE);
        registerReflectionHints(hints, ticketCompactors);
        registerProxyHints(hints, ticketCompactors);

        registerReflectionHints(hints, findSubclassesInPackage(CipherExecutor.class, CentralAuthenticationService.NAMESPACE));
    }

    private static void registerReflectionHints(final RuntimeHints hints, final Collection entries) {
        val memberCategories = new MemberCategory[]{
            MemberCategory.INTROSPECT_DECLARED_CONSTRUCTORS,
            MemberCategory.INTROSPECT_PUBLIC_CONSTRUCTORS,
            MemberCategory.INTROSPECT_DECLARED_METHODS,
            MemberCategory.INTROSPECT_PUBLIC_METHODS,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS,
            MemberCategory.INVOKE_PUBLIC_METHODS,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.PUBLIC_FIELDS};
        entries.forEach(el -> hints.reflection().registerType((Class) el, memberCategories));
    }

    private static void registerSerializationHints(final RuntimeHints hints, final Collection<Class> entries) {
        entries.forEach(el -> hints.serialization().registerType(el));
    }
}
