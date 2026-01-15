package org.apereo.cas.interrupt;

import module java.base;
import org.apereo.cas.util.NamedObject;

/**
 * This is {@link InterruptInquiryExecutionPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@FunctionalInterface
public interface InterruptInquiryExecutionPlanConfigurer extends NamedObject {
    /**
     * Configure interrupt inquiry execution plan.
     *
     * @param plan the plan
     */
    void configureInterruptInquiryExecutionPlan(InterruptInquiryExecutionPlan plan);
}
