package org.apereo.cas.services.publisher;

import org.apereo.cas.services.RegisteredService;
import org.springframework.context.ApplicationEvent;

/**
 * This is {@link CasRegisteredServiceHazelcastStreamPublisher}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CasRegisteredServiceHazelcastStreamPublisher implements CasRegisteredServiceStreamPublisher {
    @Override
    public void publish(final RegisteredService service, final ApplicationEvent event) {
             
    }
}
