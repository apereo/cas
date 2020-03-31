package org.apereo.cas.services.publisher;

import org.apereo.cas.services.RegisteredService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;

/**
 * This is {@link CasRegisteredServiceNoOpStreamPublisher}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class CasRegisteredServiceNoOpStreamPublisher implements CasRegisteredServiceStreamPublisher {

    @Override
    public void publish(final RegisteredService service, final ApplicationEvent event) {
        LOGGER.warn("CAS is NOT configured to stream and broadcast registered services over a queue. "
            + "This generally points to a configuration issue where a publisher instance is missing from the CAS runtime.");
    }
}
