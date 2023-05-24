package org.apereo.cas.nativex;

import org.apereo.cas.ticket.TicketFactoryExecutionPlanConfigurer;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlanConfigurer;
import org.apereo.cas.util.cipher.TicketGrantingCookieCipherExecutor;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link CasCoreTicketsRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasCoreTicketsRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        hints.proxies()
            .registerJdkProxy(TicketSerializationExecutionPlanConfigurer.class)
            .registerJdkProxy(TicketFactoryExecutionPlanConfigurer.class);

        hints.reflection().registerType(TicketGrantingCookieCipherExecutor.class,
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.INTROSPECT_PUBLIC_CONSTRUCTORS,
            MemberCategory.INTROSPECT_DECLARED_CONSTRUCTORS);
    }
}
