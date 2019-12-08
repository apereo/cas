package org.apereo.cas.interrupt;

/**
 * This is {@link InterruptInquiryExecutionPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@FunctionalInterface
public interface InterruptInquiryExecutionPlanConfigurer {
    /**
     * Configure interrupt inquiry execution plan.
     *
     * @param plan the plan
     */
    void configureInterruptInquiryExecutionPlan(InterruptInquiryExecutionPlan plan);
    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}
