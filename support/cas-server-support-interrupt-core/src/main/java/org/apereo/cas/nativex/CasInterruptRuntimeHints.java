package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlan;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlanConfigurer;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link CasInterruptRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasInterruptRuntimeHints implements CasRuntimeHintsRegistrar {

    @Override
    public void registerHints(final @NonNull RuntimeHints hints, final @Nullable ClassLoader classLoader) {
        registerProxyHints(hints, InterruptInquiryExecutionPlan.class, InterruptInquiryExecutionPlanConfigurer.class, InterruptInquirer.class);
        registerSerializationHints(hints, InterruptResponse.class);
    }
}
