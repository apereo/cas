package org.apereo.cas.support.saml.idp;

import org.apereo.cas.util.spring.CasEventListener;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

/**
 * This is {@link SamlIdPCasEventListener}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@FunctionalInterface
public interface SamlIdPCasEventListener extends CasEventListener {
    @EventListener
    @Async
    void handleApplicationReadyEvent(ApplicationReadyEvent event);
}
