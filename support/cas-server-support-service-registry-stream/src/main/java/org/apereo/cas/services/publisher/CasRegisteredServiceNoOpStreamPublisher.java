package org.apereo.cas.services.publisher;

import org.apereo.cas.services.RegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

/**
 * This is {@link CasRegisteredServiceNoOpStreamPublisher}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CasRegisteredServiceNoOpStreamPublisher implements CasRegisteredServiceStreamPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasRegisteredServiceNoOpStreamPublisher.class);

    @Override
    public void publish(final RegisteredService service, final ApplicationEvent event) {
        LOGGER.warn("CAS is NOT configured to stream and broadcast registered services over a queue. "
                + "This generally points to a configuration issue where a publisher instance is missing from the CAS runtime.");
    }
}
