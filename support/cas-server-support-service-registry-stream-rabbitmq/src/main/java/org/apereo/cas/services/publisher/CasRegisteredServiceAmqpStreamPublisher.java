package org.apereo.cas.services.publisher;

import org.apereo.cas.services.RegisteredService;
import org.springframework.context.ApplicationEvent;

/**
 * This is {@link CasRegisteredServiceAmqpStreamPublisher}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CasRegisteredServiceAmqpStreamPublisher implements CasRegisteredServiceStreamPublisher {
    @Override
    public void publish(final RegisteredService service, final ApplicationEvent event) {

    }
}
