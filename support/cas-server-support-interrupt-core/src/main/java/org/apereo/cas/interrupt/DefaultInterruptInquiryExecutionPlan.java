package org.apereo.cas.interrupt;

import lombok.Getter;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link DefaultInterruptInquiryExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
public class DefaultInterruptInquiryExecutionPlan implements InterruptInquiryExecutionPlan {
    private final List<InterruptInquirer> interruptInquirers = new ArrayList<>(0);

    @Override
    public void registerInterruptInquirer(final InterruptInquirer inquirer) {
        interruptInquirers.add(inquirer);
    }

    @Override
    public List<InterruptInquirer> getInterruptInquirers() {
        AnnotationAwareOrderComparator.sort(interruptInquirers);
        return interruptInquirers;
    }
}
