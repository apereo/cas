package org.apereo.cas.rest.plan;

import org.apereo.cas.rest.factory.ServiceTicketResourceEntityResponseFactory;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link DefaultServiceTicketResourceEntityResponseFactoryPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class DefaultServiceTicketResourceEntityResponseFactoryPlan implements ServiceTicketResourceEntityResponseFactoryPlan {
    private final List<ServiceTicketResourceEntityResponseFactory> factories = new ArrayList<>(0);

    @Override
    public void registerFactory(final ServiceTicketResourceEntityResponseFactory factory) {
        this.factories.add(factory);
    }

    @Override
    public Collection<ServiceTicketResourceEntityResponseFactory> getFactories() {
        AnnotationAwareOrderComparator.sort(this.factories);
        return factories;
    }
}
