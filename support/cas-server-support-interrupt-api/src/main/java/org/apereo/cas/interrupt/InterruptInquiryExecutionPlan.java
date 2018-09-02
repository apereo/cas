package org.apereo.cas.interrupt;

import java.util.List;

/**
 * This is {@link InterruptInquiryExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public interface InterruptInquiryExecutionPlan {
    /**
     * Register interrupt inquirer.
     *
     * @param inquirer the inquirer
     */
    void registerInterruptInquirer(InterruptInquirer inquirer);

    /**
     * Gets interrupt inquirers.
     *
     * @return the interrupt inquirer
     */
    List<InterruptInquirer> getInterruptInquirers();
}
