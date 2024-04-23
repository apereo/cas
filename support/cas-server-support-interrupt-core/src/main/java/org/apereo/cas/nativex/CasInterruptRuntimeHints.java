package org.apereo.cas.nativex;

import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlan;
import org.apereo.cas.interrupt.InterruptInquiryExecutionPlanConfigurer;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;

/**
 * This is {@link CasInterruptRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasInterruptRuntimeHints implements CasRuntimeHintsRegistrar {

    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        registerProxyHints(hints, InterruptInquiryExecutionPlan.class, InterruptInquiryExecutionPlanConfigurer.class, InterruptInquirer.class);
        registerSerializationHints(hints, InterruptResponse.class);
    }
}
