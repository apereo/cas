package org.apereo.cas.nativex;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactoryExecutionPlanConfigurer;
import org.apereo.cas.ticket.registry.TicketCompactor;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.pubsub.QueueableTicketRegistry;
import org.apereo.cas.ticket.registry.pubsub.queue.QueueableTicketRegistryMessageReceiver;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlanConfigurer;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.crypto.PropertyBoundCipherExecutor;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.thread.Cleanable;
import lombok.val;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.integration.support.locks.LockRegistry;

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
        registerProxyHints(hints, CipherExecutor.class);

        registerSpringProxyHints(hints, Cleanable.class, TicketRegistry.class);
        registerSpringProxyHints(hints, PropertyBoundCipherExecutor.class, CipherExecutor.class);
        registerSpringProxyHints(hints, QueueableTicketRegistry.class, TicketRegistry.class);
        registerSpringProxyHints(hints, AutoCloseable.class, DisposableBean.class, TicketRegistry.class);

        registerSerializationHints(hints, findSubclassesInPackage(Ticket.class, CentralAuthenticationService.NAMESPACE));
        val expirationPolicyClasses = findSubclassesInPackage(ExpirationPolicy.class, CentralAuthenticationService.NAMESPACE);
        registerSerializationHints(hints, expirationPolicyClasses);
        registerReflectionHints(hints, expirationPolicyClasses);

        val ticketCompactors = findSubclassesInPackage(TicketCompactor.class, CentralAuthenticationService.NAMESPACE);
        registerReflectionHints(hints, ticketCompactors);
        registerProxyHints(hints, TicketCompactor.class);
        registerProxyHints(hints, LockRegistry.class);

        registerReflectionHints(hints, findSubclassesInPackage(CipherExecutor.class, CentralAuthenticationService.NAMESPACE));
        registerReflectionHints(hints, findSubclassesInPackage(QueueableTicketRegistryMessageReceiver.class, CentralAuthenticationService.NAMESPACE));
    }
}
